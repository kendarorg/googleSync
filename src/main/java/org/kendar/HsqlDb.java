package org.kendar;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HsqlDb {
    private String connection;
    private String login;
    private String pwd;

    public HsqlDb(String connection, String login, String pwd) {
        this.connection = connection;//+";shutdown=true;hsqldb.write_delay=false;";
        this.login = login;
        this.pwd = pwd;
        try {
            //Class.forName("org.hsqldb.jdbcDriver");
            Class.forName("org.sqlite.JDBC");
            intialize();
        } catch (ClassNotFoundException e) {
            //e.printStackTrace();
        } catch (SQLException e) {
            //e.printStackTrace();
        }
    }

    private void intialize() throws SQLException {
        Connection conn = connect();
        DatabaseMetaData meta = conn.getMetaData();
        //Statement st1 = conn.createStatement();
        //st1.execute("SET DATABASE DEFAULT TABLE TYPE CACHED ");
        Statement st = conn.createStatement();
        st.execute("CREATE TABLE IF NOT EXISTS MD5S (ID TEXT PRIMARY KEY, CHECKSUM TEXT NOT NULL," +
                " CREATION_DATE TEXT  NOT NULL, LAST_UPDATE TEXT  NOT NULL)");
        st.close();

        //st = conn.createStatement();
        //st.execute("SET TABLE MD5S SOURCE 'md5s.csv'");
        //st.close();



        conn.commit();
        conn.close();

    }

    private Connection connect() throws SQLException {
        Connection res = DriverManager.getConnection(connection,login,pwd);
        res.setAutoCommit(true);
        return res;
    }

    public String getMd5(String id) throws Exception {
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

    private String now() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
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
    }
}
