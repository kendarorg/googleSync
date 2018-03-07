package org.enel;

import org.enel.entities.DirectoryStatus;
import org.enel.entities.DriveItem;
import org.enel.utils.GDException;
import org.enel.utils.ThrowingFunction;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
            //st.execute("CREATE TABLE IF NOT EXISTS MD5S (ID TEXT PRIMARY KEY, CHECKSUM TEXT NOT NULL," +
            //        " CREATION_DATE TEXT  NOT NULL, LAST_UPDATE TEXT  NOT NULL)");
            st.execute("CREATE TABLE IF NOT EXISTS FOLDER_STATUSES (ID TEXT PRIMARY KEY, PATH TEXT NOT NULL," +
                    " CREATION_DATE TEXT  NOT NULL, LAST_UPDATE TEXT  NOT NULL, TAG TEXT NULL, DIR_ID TEXT NOT NULL)");
            st.close();

            conn.commit();
            conn.close();
        }catch(SQLException e){
            throw new GDException("060",e);
        }

    }

    private Connection connect() throws SQLException {
        Connection res = DriverManager.getConnection(connection,login,pwd);
        res.setAutoCommit(true);
        return res;
    }


    private String now() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }

    public DirectoryStatus getDirStatus(final DriveItem item, final String realPath) {
        return doExecute((c)->{
            DirectoryStatus result =null;
            Statement st = c.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM FOLDER_STATUSES WHERE PATH='" + realPath + "'");
            for (; rs.next(); ) {
                result = new DirectoryStatus();
                result.setId(UUID.fromString(rs.getString("ID")));
                result.setLastUpdate(rs.getString("TAG"));
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

    public void saveDirStatus(DirectoryStatus item,boolean dryRun) throws GDException {
        if(dryRun)return;
        doExecute((c)->{
            String date = now();
            Statement st = c.createStatement();
            if(item.getId()==null) {
                item.setId(UUID.randomUUID());
                st.executeUpdate("INSERT INTO FOLDER_STATUSES (ID,PATH,CREATION_DATE,LAST_UPDATE,TAG,DIR_ID) " +
                        " VALUES ('" + item.getId()
                        + "','" + item.getRealPath() + "','" + date + "','" + date + "','" + item.getLastUpdate() + "','" +
                        item.getDirectoryId() + "')");
            }else{
                st.executeUpdate("UPDATE FOLDER_STATUSES SET" +
                        " PATH='"+item.getRealPath()+"',LAST_UPDATE='"+date+"',TAG='"+item.getLastUpdate()+"') WHERE " +
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
