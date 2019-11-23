import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request {
	private static final String driver = "com.mysql.cj.jdbc.Driver";
	private static final String url = "jdbc:mysql://localhost/airpollution?characterEncoding=UTF-8&serverTimezone=UTC"; // �ּ�, db��
	private static final String id = "root";
	private static final String password = "468315";

	public static void inputData(String path, String name) { // ���ϵ����� ������ �Է� �޼ҵ�
		
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE IF NOT EXISTS `")
		.append(name)
		.append("` (`id` INT NOT NULL PRIMARY KEY AUTO_INCREMENT, `date` VARCHAR(10) NOT NULL, `place` VARCHAR(15) NOT NULL, `nitrogen` VARCHAR(10) NOT NULL, `ozone` VARCHAR(10) NOT NULL, `carbon` VARCHAR(10) NOT NULL, `gas` VARCHAR(10) NOT NULL, `dust` VARCHAR(10) NOT NULL, `ultradust` VARCHAR(10) NOT NULL);").toString();

		try (BufferedReader br = new BufferedReader(new FileReader(new File(path)));
				Connection conn=getConnection();
				Statement stmt = conn.createStatement();){
			
			String line;
			
			stmt.execute(sb.toString());
			
			int len = br.readLine().split(",").length; // ù�� �ѱ�� �� ���� ������. ���������� ó��
			while((line = br.readLine()) != null) {
				int i=0;
				sb.setLength(0);
				String[] datas = line.split(",");
				
				sb.append("INSERT INTO `")
					.append(name)
					.append("` (`date`, `place`, `nitrogen`, `ozone`, `carbon`, `gas`, `dust`, `ultradust`) VALUES('")
					.append(datas[i++]).append("', '").append(datas[i++]);
					
				while(i<datas.length) sb.append("', '").append(datas[i++]);
				while(i++<len) sb.append("', '"); // �ް��� ������
				sb.append("');");
				
				stmt.execute(sb.toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public static Connection getConnection() throws ClassNotFoundException, SQLException {
		Class.forName(Request.driver);
		return DriverManager.getConnection(Request.url, Request.id, Request.password); // url, ���̵�, ���
	
	}
	
	public static Datas openData(String name, String start, String end) { // ���������� ����
		
		List<Data> datas = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		
		sb.append("SELECT * FROM `").append(name).append("`");
		if(start !=null && end != null)
			sb.append("WHERE ").append(start).append("<=`date` AND `date` <=").append(end);
		sb.append(";");
		
		try (Connection conn=getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sb.toString());) {
			while(rs.next()) {
				datas.add(new Data(rs.getString(2),
						rs.getString(3),
						rs.getString(4),
						rs.getString(5),
						rs.getString(6),
						rs.getString(7),
						rs.getString(8),
						rs.getString(9)));
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} 

		String s = null, e = null; 
		if(!datas.isEmpty()) {
			datas.sort((x, y)->x.getDate().compareTo(y.getDate())); // ��¥������ ����
			s=datas.get(0).getDate();
			e=datas.get(datas.size()-1).getDate();
		}
		
		Datas d = new Datas(name, datas, s, e);
		
		return d;
	}
	
	public static void remove(String str) { // ���� ���̺� ����
		StringBuilder sb = new StringBuilder();

		sb.append("DROP TABLE `").append(str).append("`");
		
		try (Connection conn=getConnection();
				Statement stmt = conn.createStatement();){

			stmt.execute(sb.toString());
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} 
	}
	
	public static void outputData(String str, String path) { // ���������� ���Ϸ� ���
		
		StringBuilder sb = new StringBuilder();

		sb.append("SELECT * FROM `").append(str).append("`");
		
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(path, true));
				Connection conn=getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sb.toString());){

			bw.write("�����Ͻ�,�����Ҹ�,�̻�ȭ���ҳ�(ppm),������(ppm),�̻�ȭź�ҳ�(ppm),��Ȳ�갡��(ppm),�̼�����(��/��),�ʹ̼�����(��/��)");
			while(rs.next()) {
				sb.setLength(0);
				bw.newLine();
				bw.write(sb.append(rs.getString(2)).append(",")
						.append(rs.getString(3)).append(",")
						.append(rs.getString(4)).append(",")
						.append(rs.getString(5)).append(",")
						.append(rs.getString(6)).append(",")
						.append(rs.getString(7)).append(",")
						.append(rs.getString(8)).append(",")
						.append(rs.getString(9)).toString());
			}
		} catch (IOException e){
			e.printStackTrace();
		}catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} 
	}
	
	public static String[] showTables() { // ���̺� ��� ��ȯ
		ArrayList<String> str = new ArrayList<>();
	
		try (Connection conn=getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery("SHOW TABLES;");) {
			while(rs.next()) str.add(rs.getString(1));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return str.toArray(new String[str.size()]);
	}
	
}
