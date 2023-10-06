package com.github.coderodde.web.viewcounter;

import com.google.gson.Gson;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(value = "/count")
public final class GetViewStatisticsServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request,
                                  HttpServletResponse response)
            throws ClassNotFoundException,
                   ServletException,
                   NamingException,
                   SQLException, 
                   IOException {
        
        Class.forName("com.mysql.jdbc.Driver"); 
        
        response.setContentType("text/html;charset=UTF-8");
        
        Connection connection = 
            DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/view_counter_db?useSSL=false",
                "root",
                "");
        
        Statement statement = connection.createStatement();
        ResultSet resultSet = 
                statement.executeQuery("SELECT views FROM view_counter;");
        
        resultSet.next();
        int views = resultSet.getInt(1);
        
        ResponseObject responseObject = new ResponseObject();
        responseObject.views = views + 1;
        
        PreparedStatement preparedStatement = 
                connection.prepareStatement(
                        "UPDATE view_counter SET views = ?;");
        
        preparedStatement.setInt(1, views + 1);
        preparedStatement.execute();
        
        String remoteAddr = request.getHeader("X-FORWARDED-FOR");
        
        if (remoteAddr == null || "".equals(remoteAddr)) {
            remoteAddr = request.getRemoteAddr();
        }
        
        preparedStatement =
                connection.prepareStatement(
                        "INSERT IGNORE INTO unique_ips VALUES(?);");
        
        preparedStatement.setString(1, remoteAddr);
        preparedStatement.execute();
        
        statement = connection.createStatement();
        resultSet = statement.executeQuery("SELECT COUNT(*) FROM unique_ips;");
        resultSet.next();
        
        responseObject.uniqueIPs = resultSet.getInt(1);
        response.getWriter().print(new Gson().toJson(responseObject));
    }
    
    @Override
    protected void doGet(HttpServletRequest request, 
                         HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            processRequest(request, response);
        } catch (NamingException ex) {
            Logger.getLogger(
                    GetViewStatisticsServlet.class.getName())
                    .log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(
                    GetViewStatisticsServlet.class.getName())
                    .log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(GetViewStatisticsServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, 
                          HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            processRequest(request, response);
        } catch (NamingException ex) {
            Logger.getLogger(
                    GetViewStatisticsServlet.class.getName())
                    .log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(
                    GetViewStatisticsServlet.class.getName())
                    .log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(GetViewStatisticsServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static final class ResponseObject {
        public int views;
        public int uniqueIPs;
    }
}
