package org.enel;

import org.enel.entities.DirectoryStatus;
import org.enel.entities.DriveItem;
import org.enel.entities.ScheduledOperation;
import org.enel.utils.GDException;
import org.enel.utils.ThrowingFunction;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

public class GDDatabase {
    private String connection;
    private String login;
    private String pwd;

    public GDDatabase() {

        try {
            //Class.forName("org.hsqldb.jdbcDriver");
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            //e.printStackTrace();
        }
    }

    public void intialize(String connection, String login, String pwd) throws GDException {
        this.connection = connection;//+";shutdown=true;hsqldb.write_delay=false;";
        this.login = login;
        this.pwd = pwd;
        try {
            Connection conn = connect();
            DatabaseMetaData meta = conn.getMetaData();
            Statement st = conn.createStatement();
            st.execute("CREATE TABLE IF NOT EXISTS FOLDER_STATUSES (ID TEXT PRIMARY KEY, PATH TEXT NOT NULL," +
                    " CREATION_DATE TEXT  NOT NULL, LAST_UPDATE TEXT  NOT NULL, LAST_UPDATE_TIME TEXT  NOT NULL, TAG TEXT NULL, DIR_ID TEXT NOT NULL)");
            st.close();

            st = conn.createStatement();
            st.execute("CREATE TABLE IF NOT EXISTS GOOGLE_DATA (ID TEXT PRIMARY KEY, REMOTE_PATH TEXT NOT NULL," +
                    " CREATION_DATE TEXT  NOT NULL, LAST_UPDATE TEXT  NOT NULL, NAME TEXT NULL, MD5 TEXT NOT NULL," +
                    "  IS_DIR BOOLEAN NOT NULL CHECK (IS_DIR IN (0,1)))");
            st.close();

            st = conn.createStatement();
            st.execute("CREATE TABLE IF NOT EXISTS SCHEDULED_OPERATION (ID TEXT PRIMARY KEY, TODO NUMBER NOT NULL," +
                    " LOCAL_PATH TEXT  NOT NULL, REMOTE_PATH TEXT  NOT NULL," +
                    " COMPLETED BOOLEAN NOT NULL CHECK (IS_DIR IN (0,1))," +
                    "  IS_DIR BOOLEAN NOT NULL CHECK (IS_DIR IN (0,1)))");
            st.close();


            conn.commit();
            conn.close();
        }catch(SQLException e){
            throw new GDException("060",e);
        }

    }

    public void scheduleOperation(ScheduledOperation operation){
        operation.setId(UUID.randomUUID());
        doExecute((c)->{
            Statement st = c.createStatement();
            st.executeUpdate("INSERT INTO SCHEDULED_OPERATION (ID,TODO,LOCAL_PATH,REMOTE_PATH,COMPLETED,IS_DIR) " +
                " VALUES ('" +
                    operation.getId()+ "',''" +
                    operation.getOperation().toInt()+ "',''" +
                    operation.getLocalPath()+ "',''" +
                    operation.getRemptePath()+ "',0,0," +
                    (operation.isDir()?1:0)+ ")");
            st.close();
            return null;
        });
    }

    private Connection connect() throws SQLException {
        Connection res = DriverManager.getConnection(connection,login,pwd);
        res.setAutoCommit(true);
        return res;
    }

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    public DirectoryStatus getDirStatus(final DriveItem item, final String realPath) {
        return doExecute((c)->{
            DirectoryStatus result =null;
            Statement st = c.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM FOLDER_STATUSES WHERE PATH='" + realPath + "'");
            for (; rs.next(); ) {
                result = new DirectoryStatus();
                result.setId(UUID.fromString(rs.getString("ID")));
                result.setLastUpdate(rs.getString("TAG"));

                Instant lastuUpdateTime = sdf.parse(rs.getString("LAST_UPDATE_TIME")).toInstant().truncatedTo(ChronoUnit.SECONDS);
                result.setLastUpdateTime(lastuUpdateTime);
                result.setDirectoryPath(item.getFullPath());
                result.setDirectoryId(rs.getString("DIR_ID"));
                result.setRealPath(rs.getString("PATH"));
                return result;
                //return rs.getString("CHECKSUM");
            }
            result = new DirectoryStatus();
            result.setDirectoryPath(item.getFullPath());
            result.setRealPath(realPath);
            result.setDirectoryId(item.getId());
            return result;
        });
    }

    public void saveDriveItem(DriveItem item,boolean dryRun) throws GDException {
        if(dryRun)return;
        doExecute((c)->{
            Statement st = c.createStatement();
            int updated = st.executeUpdate("UPDATE GOOGLE_DATA SET" +
                    " REMOTE_PATH='"+item.getFullPath()+"'," +
                    " NAME='"+item.getName()+"'," +
                    " MD5='"+item.getMd5()+"'," +
                    " IS_DIR='"+(item.isDir()?1:0)+"'," +
                    " CREATION_DATE='"+item.getCreatedTime()+"'," +
                    " LAST_UPDATE='"+item.getModifiedTime()+"') WHERE " +
                    " ID='"+item.getId()+"'");
            if(updated==0){
                st.executeUpdate("INSERT INTO GOOGLE_DATA (ID,REMOTE_PATH,CREATION_DATE,LAST_UPDATE,NAME,MD5,IS_DIR) " +
                        " VALUES ('" +
                        item.getId()+ "',''" +
                        item.getFullPath()+ "',''" +
                        item.getCreatedTime()+ "',''" +
                        item.getModifiedTime()+ "',''" +
                        item.getName()+ "','','" +
                        item.getMd5()+ "','','" +
                        (item.isDir()?1:0)+ "')");
            }
            st.close();
            return null;
        });
    }

    public void saveDirStatus(DirectoryStatus item,boolean dryRun) throws GDException {
        if(dryRun)return;
        doExecute((c)->{
            Instant now = Instant.now();
            Statement st = c.createStatement();
            if(item.getId()==null) {
                item.setId(UUID.randomUUID());
                st.executeUpdate("INSERT INTO FOLDER_STATUSES (ID,PATH,CREATION_DATE,LAST_UPDATE,TAG,LAST_UPDATE_TIME,DIR_ID) " +
                        " VALUES ('" + item.getId()
                        + "','" + item.getRealPath() + "','" + now + "','" + now + "','" +
                        item.getLastUpdate() + "','" +
                        item.getLastUpdateTime() + "','" +
                        item.getDirectoryId() + "')");
            }else{
                st.executeUpdate("UPDATE FOLDER_STATUSES SET" +
                        " PATH='"+item.getRealPath()+"',LAST_UPDATE='"+now+"',TAG='"+item.getLastUpdate()+"'," +
                        "LAST_UPDATE_TIME='"+item.getLastUpdateTime()+"') WHERE " +
                        " ID='"+item.getId()+"'");
            }
            st.close();
            return null;
        });
    }

    private <T> T doExecute(ThrowingFunction<Connection,T> function){
        Connection conn = null;
        try {
            conn = connect();
            return function.apply(conn);
        }catch(Exception ex){
            return null;
        }finally{
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*public String getMd5(String id) throws Exception {
        Connection conn = null;
        try {
            conn = connect();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT CHECKSUM FROM MD5S WHERE ID='" + id + "'");
            for (; rs.next(); ) {
                return rs.getString("CHECKSUM");
            }
            return null;
        }catch(Exception ex){
            return null;
        }finally{
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void addMd5(String id, String md5) {

        Connection conn = null;
        try {
            conn = connect();
            String date = now();
            Statement st = conn.createStatement();
            st.executeUpdate("INSERT INTO MD5S (ID,CHECKSUM,CREATION_DATE,LAST_UPDATE) " +
                    " VALUES ('"+id+"','"+md5+"','"+date+"','"+date+"')");
            st.close();
        }catch(Exception ex){
            System.out.println(ex);
        }finally{
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public void updateMd5(String id, String md5) {
        Connection conn = null;
        try {
            conn = connect();
            String date = now();
            Statement st = conn.createStatement();
            st.executeUpdate("UPDATE MD5S SET CHECKSUM='"+md5+"',LAST_UPDATE='"+date+"') WHERE " +
                    " ID='"+id+"'");
            st.close();
        }catch(Exception ex){

        }finally{
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }*/
}
