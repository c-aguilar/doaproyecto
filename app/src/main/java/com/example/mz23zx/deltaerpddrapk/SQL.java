package com.example.mz23zx.deltaerpddrapk;

import android.os.StrictMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SQL {
    public SQL(String connection_string) {
    }
    public static String GlobalConnectionString;
    public Connection _connection = null;
    public static SQL Current(){
        SQL _sql = new SQL(SQL.GlobalConnectionString);
        return _sql;
    }
    private Boolean Open() {
        for(int i=1;i<=3;i++){
            try{
                StrictMode.ThreadPolicy policy=new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
                Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();
                _connection = DriverManager.getConnection(String.format("jdbc:jtds:sqlserver://%1$s;instance=%2$s;databaseName=%3$s;user=%4$s;password=%5$s;",GlobalVariables.Server,GlobalVariables.Instance,GlobalVariables.Database,GlobalVariables.UID,GlobalVariables.Password));
                return true;
            }catch (Exception e){

            }
        }
        return false;
    }

    private void Close(){
        try
        {
            _connection.close();
        }
        catch (SQLException e){
        }
    }

    public Boolean Available(){
        if(Open()){
            Close();
            return true;
        }
        else{
            return false;
        }
    }

    public Boolean Execute(String query){
        if(Open()){
            try{
                PreparedStatement pat= _connection.prepareStatement(query);
                pat.execute();
                Close();
                return true;
            }catch (Exception e){
                Close();
                return false;
            }
        }else{
            return false;
        }
    }

    public String GetString(String query){
        if (Open()){
            try{
                Statement st = _connection.createStatement();
                ResultSet rs = st.executeQuery(query);
                if (rs.next()){
                    String str =  rs.getString(1);
                    rs.close();
                    Close();
                    return str;
                }
                else
                {
                    Close();
                    return  "";
                }
            } catch (SQLException e){
                Close();
                return "";
            }
        }else{
            return "";
        }
    }

    public String GetString(String select, String intotable, String where, Object equals_to){
        if (Open()){
            try{
                String query = String.format("SELECT TOP 1 %1$s FROM %2$s WHERE %3$s = ?",select, intotable,where);
                PreparedStatement pst = _connection.prepareStatement(query);
                pst.setObject(1, equals_to);
                ResultSet rs = pst.executeQuery();
                if (rs.next()){
                    String str =  rs.getString(1);
                    rs.close();
                    Close();
                    return str;
                }
                else
                {
                    Close();
                    return "";
                }
            } catch (SQLException e){
                Close();
                return "";
            }
        }else{
            return "";
        }
    }

    public String GetString(String select,String intotable,String[] where,Object[] equals_to){
        if (Open()){
            try{
                String query = String.format("SELECT TOP 1 %1$s FROM %2$s",select, intotable);
                if(where.length > 0){
                    query += " WHERE ";
                }
                for(int i=0;i<where.length;i++){
                    query += where[i] + " = ?";
                    if (i < where.length-1){
                        query += " AND ";
                    }
                }
                PreparedStatement pst = _connection.prepareStatement(query);
                for(int i=0;i<equals_to.length;i++){
                    pst.setObject(i+1, equals_to[i]);
                }
                ResultSet rs = pst.executeQuery();
                if (rs.next()){
                    String str =  rs.getString(1);
                    rs.close();
                    Close();
                    return str;
                }
                else
                {
                    Close();
                    return "";
                }
            } catch (SQLException e){
                Close();
                return "";
            }
        }else{
            return "";
        }
    }

    public List<Map<String, Object>> GetTable(String query){
        if (Open()){
            try{
                Statement st = _connection.createStatement();
                ResultSet rs = st.executeQuery(query);
                ResultSetMetaData md = rs.getMetaData();
                int columns = md.getColumnCount();
                List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
                while (rs.next()){
                    Map<String, Object> row = new HashMap<String, Object>(columns);
                    for(int i = 1; i <= columns; ++i){
                        row.put(md.getColumnName(i).toLowerCase(), rs.getObject(i));
                    }
                    rows.add(row);
                }
                rs.close();
                Close();
                return rows;

            } catch (SQLException e){
                Close();
                return null;
            }
        }else{
            return null;
        }
    }

    public List<Map<String, Object>> GetTable(String select,String fromtable,String[] where,Object[] equals_to){
        if (Open()){
            try{
                String query = String.format("SELECT %1$s FROM %2$s",select, fromtable);
                if(where.length > 0){
                    query += " WHERE ";
                }
                for(int i=0;i<where.length;i++){
                    query += where[i] + " = ?";
                    if (i < where.length-1){
                        query += " AND ";
                    }
                }
                PreparedStatement pst = _connection.prepareStatement(query);
                for(int i=0;i<equals_to.length;i++){
                    pst.setObject(i+1, equals_to[i]);
                }
                ResultSet rs = pst.executeQuery();
                ResultSetMetaData md = rs.getMetaData();
                int columns = md.getColumnCount();
                List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
                while (rs.next()){
                    Map<String, Object> row = new HashMap<String, Object>(columns);
                    for(int i = 1; i <= columns; ++i){
                        row.put(md.getColumnName(i).toLowerCase(), rs.getObject(i));
                    }
                    rows.add(row);
                }
                rs.close();
                Close();
                return rows;

            } catch (SQLException e){
                Close();
                return null;
            }
        }else{
            return null;
        }
    }

    public Map<String, Object> GetRecord(String query){
        if (Open()){
            try{
                Statement st = _connection.createStatement();
                ResultSet rs = st.executeQuery(query);
                ResultSetMetaData md = rs.getMetaData();
                int columns = md.getColumnCount();
                Map<String, Object> row = new HashMap<String, Object>(columns);
                if (rs.next() == true){
                    for(int i = 1; i <= columns; ++i){
                        row.put(md.getColumnName(i).toLowerCase(), rs.getObject(i));
                    }
                }
                rs.close();
                Close();
                return row;
            } catch (SQLException e){
                Close();
                return null;
            }
        }else{
            return null;
        }
    }

    public Map<String, Object> GetRecord(String select,String fromtable,String[] where,Object[] equals_to){
        if (Open()){
            try{
                String query = String.format("SELECT %1$s FROM %2$s",select, fromtable);
                if(where.length > 0){
                    query += " WHERE ";
                }
                for(int i=0;i<where.length;i++){
                    query += where[i] + " = ?";
                    if (i < where.length-1){
                        query += " AND ";
                    }
                }
                PreparedStatement pst = _connection.prepareStatement(query);
                for(int i=0;i<equals_to.length;i++){
                    pst.setObject(i+1, equals_to[i]);
                }
                ResultSet rs = pst.executeQuery();
                ResultSetMetaData md = rs.getMetaData();
                int columns = md.getColumnCount();
                Map<String, Object> row = new HashMap<String, Object>(columns);
                if (rs.next() == true){
                    for(int i = 1; i <= columns; ++i){
                        row.put(md.getColumnName(i).toLowerCase(), rs.getObject(i));
                    }
                }
                rs.close();
                Close();
                return row;
            } catch (SQLException e){
                Close();
                return null;
            }
        }else{
            return null;
        }
    }

    public Boolean Exists(String query){
        if (Open()){
            try{
                Statement st = _connection.createStatement();
                ResultSet rs = st.executeQuery(query);
                if (rs.next()){
                    rs.close();
                    Close();
                    return true;
                }
                else
                {
                    Close();
                    return false;
                }
            } catch (SQLException e){
                Close();
                return false;
            }
        }else{
            return false;
        }
    }

    public Boolean Exists(String intotable,String where,Object equals_to){
        if (Open()){
            try{
                String query = String.format("SELECT TOP 1 '' FROM %1$s WHERE %2$s = ?", intotable,where);
                PreparedStatement pst = _connection.prepareStatement(query);
                pst.setObject(1, equals_to);
                ResultSet rs = pst.executeQuery();
                if (rs.next()){
                    rs.close();
                    Close();
                    return true;
                }
                else
                {
                    Close();
                    return false;
                }
            } catch (SQLException e){
                Close();
                return false;
            }
        }else{
            return false;
        }
    }

    public Boolean Exists(String intotable,String[] where,Object[] equals_to){
        if (Open()){
            try{
                String query = String.format("SELECT TOP 1 '' FROM %1$s", intotable);
                if(where.length > 0){
                    query += " WHERE ";
                }
                for(int i=0;i<where.length;i++){
                    query += where[i] + " = ?";
                    if (i < where.length-1){
                        query += " AND ";
                    }
                }
                PreparedStatement pst = _connection.prepareStatement(query);
                for(int i=0;i<equals_to.length;i++){
                    pst.setObject(i+1, equals_to[i]);
                }
                ResultSet rs = pst.executeQuery();
                if (rs.next()){
                    rs.close();
                    Close();
                    return true;
                }
                else
                {
                    Close();
                    return false;
                }
            } catch (SQLException e){
                Close();
                return false;
            }
        }else{
            return false;
        }
    }

    public Double GetDouble(String query){
        if (Open()){
            try{
                Statement st = _connection.createStatement();
                ResultSet rs = st.executeQuery(query);
                if (rs.next()){
                    Double dbl =  rs.getDouble(1);
                    rs.close();
                    Close();
                    return dbl;
                }
                else
                {
                    Close();
                    return null;
                }
            } catch (SQLException e){
                Close();
                return null;
            }
        }else{
            return null;
        }
    }


    public Float GetFloat(String query){
        if (Open()){
            try{
                Statement st = _connection.createStatement();
                ResultSet rs = st.executeQuery(query);
                if (rs.next()){
                    Float dbl =  rs.getFloat(1);
                    rs.close();
                    Close();
                    return dbl;
                }
                else
                {
                    Close();
                    return null;
                }
            } catch (SQLException e){
                Close();
                return null;
            }
        }else{
            return null;
        }
    }

    public Float GetFloat(String select,String intotable,String[] where,Object[] equals_to){
        if (Open()){
            try{
                String query = String.format("SELECT TOP 1 %1$s FROM %2$s",select, intotable);
                if(where.length > 0){
                    query += " WHERE ";
                }
                for(int i=0;i<where.length;i++){
                    query += where[i] + " = ?";
                    if (i < where.length-1){
                        query += " AND ";
                    }
                }
                PreparedStatement pst = _connection.prepareStatement(query);
                for(int i=0;i<equals_to.length;i++){
                    pst.setObject(i+1, equals_to[i]);
                }
                ResultSet rs = pst.executeQuery();
                if (rs.next()){
                    Float i =  rs.getFloat(1);
                    rs.close();
                    Close();
                    return i;
                }
                else
                {
                    Close();
                    return null;
                }
            } catch (SQLException e){
                Close();
                return null;
            }
        }else{
            return null;
        }
    }

    public Integer GetInteger(String query){
        if (Open()){
            try{
                Statement st = _connection.createStatement();
                ResultSet rs = st.executeQuery(query);
                if (rs.next()){
                    Integer dbl =  rs.getInt(1);
                    rs.close();
                    Close();
                    return dbl;
                }
                else
                {
                    Close();
                    return null;
                }
            } catch (SQLException e){
                Close();
                return null;
            }
        }else{
            return null;
        }
    }

    public Integer GetInteger(String select,String intotable,String[] where,Object[] equals_to){
        if (Open()){
            try{
                String query = String.format("SELECT TOP 1 %1$s FROM %2$s",select, intotable);
                if(where.length > 0){
                    query += " WHERE ";
                }
                for(int i=0;i<where.length;i++){
                    query += where[i] + " = ?";
                    if (i < where.length-1){
                        query += " AND ";
                    }
                }
                PreparedStatement pst = _connection.prepareStatement(query);
                for(int i=0;i<equals_to.length;i++){
                    pst.setObject(i+1, equals_to[i]);
                }
                ResultSet rs = pst.executeQuery();
                if (rs.next()){
                    Integer i =  rs.getInt(1);
                    rs.close();
                    Close();
                    return i;
                }
                else
                {
                    Close();
                    return null;
                }
            } catch (SQLException e){
                Close();
                return null;
            }
        }else{
            return null;
        }
    }

    public Boolean Insert(String intotable,String [] columns, Object [] values){
        if (Open()){
            try{
                String query = String.format("INSERT INTO %1$s (%2$s) VALUES (%3$s);", intotable,String.join(",", columns),String.join(",",ParametersArray(values.length)));
                PreparedStatement pst = _connection.prepareStatement(query);
                for(int i = 0; i < values.length; i++){
                    pst.setObject(i+1, values[i]);
                }
                pst.execute();
                Close();
                return true;
            }catch (SQLException e){
                return false;
            }
        }else{
            return false;
        }
    }

    public Boolean Insert(String intotable,String  column, Object  value){
        if (Open()){
            try{
                String query = String.format("INSERT INTO %1$s (%2$s) VALUES (?);", intotable,column);
                PreparedStatement pst = _connection.prepareStatement(query);
                pst.setObject(1, value);
                pst.execute();
                Close();
                return true;
            }catch (SQLException e){
                return false;
            }
        }else{
            return false;
        }
    }

    public Boolean Update(String table,String[] set_columns, Object[] values,String[] where, Object[] equals_to){
        if (Open()){
            try{
                String query = String.format("UPDATE %1$s SET ",table);
                for(int i=0;i<set_columns.length;i++){
                    query += set_columns[i] + " = ?";
                    if(i < set_columns.length-1){
                        query += ",";
                    }
                }
                if(where.length > 0){
                    query += " WHERE ";
                }
                for(int i=0;i<where.length;i++){
                    query += where[i] + " = ?";
                    if (i < where.length-1){
                        query += " AND ";
                    }
                }
                PreparedStatement pst = _connection.prepareStatement(query);
                for(int i=0;i<values.length;i++){
                    pst.setObject(i+1, values[i]);
                }
                for(int i=values.length;i  < values.length + equals_to.length;i++){
                    pst.setObject(i+1, equals_to[i - values.length]);
                }
                pst.execute();
                Close();
                return true;
            }catch (Exception e){
                return false;
            }
        }
        else{
            return false;
        }
    }

    public Boolean Update(String table,String set_column, Object value,String where, Object equals_to){
        if (Open()){
            try{
                String query = String.format("UPDATE %1$s SET %2$s = ? WHERE %3$s = ?",table,set_column,where);
                PreparedStatement pst = _connection.prepareStatement(query);
                pst.setObject(1, value);
                pst.setObject(2, equals_to);
                pst.execute();
                Close();
                return true;
            }catch (Exception e){
                return false;
            }
        }
        else{
            return false;
        }
    }


    private String [] ParametersArray(Integer count){
        String[] array = new String[count];
        for (int i = 0;i < count; i++){
            array[i] = "?";
        }
        return array;
    }
}
