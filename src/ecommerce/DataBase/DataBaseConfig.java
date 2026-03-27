package ecommerce.DataBase;

public class DataBaseConfig {
    public static final String Nom_Driver="com.mysql.cj.jdbc.Driver";
    public static final String IPServeur="localhost";
    public static final String PORT="3306";
    public static final String DataBaseName="ecommerce";
    public static final String url_db="jdbc:mysql://"+IPServeur+":"+PORT+"/"+DataBaseName;
    public static final String username="root";
    public static final String PASSWORD="";
}