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
import java.util.List;

public class Request {
	private static final String driver = "com.mysql.cj.jdbc.Driver";
	private static final String url = "jdbc:mysql://localhost/airpollution?characterEncoding=UTF-8&serverTimezone=UTC"; // 주소, db명
	private static final String id = "root";
	private static final String password = "468315";

	public static void inputData(String path, String name) {
		
		// name(테이블명)이 이미 있을 경우 예외처리, 없을 경우 CREATE TABLE
			
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE IF NOT EXISTS `")
		.append(name)
		.append("` (`id` INT NOT NULL PRIMARY KEY AUTO_INCREMENT, `date` VARCHAR(10) NOT NULL, `place` VARCHAR(15) NOT NULL, `nitrogen` VARCHAR(10) NOT NULL, `ozone` VARCHAR(10) NOT NULL, `carbon` VARCHAR(10) NOT NULL, `gas` VARCHAR(10) NOT NULL, `dust` VARCHAR(10) NOT NULL, `ultradust` VARCHAR(10) NOT NULL);").toString();

		try (BufferedReader br = new BufferedReader(new FileReader(new File(path)));
				Connection conn=getConnection();
				Statement stmt = conn.createStatement();){
			
			String line;
			
			stmt.execute(sb.toString());
			
			// 파일 이미 있으면 별도의 예외처리
			
			int len = br.readLine().split(",").length; // 첫행 넘기고 열 길이 가져옴. 누락데이터 처리
			while((line = br.readLine()) != null) {
				// 파일 읽는 속도 개선 필요
				int i=0;
				sb.setLength(0);
				String[] datas = line.split(",");
				
				// 누락데이터 그냥 공백으로 넣음
				sb.append("INSERT INTO `")
					.append(name)
					.append("` (`date`, `place`, `nitrogen`, `ozone`, `carbon`, `gas`, `dust`, `ultradust`) VALUES('")
					.append(datas[i++]).append("', '").append(datas[i++]);
					
				while(i<datas.length) sb.append("', '").append(datas[i++]);
				while(i++<len) sb.append("', '"); // 뒷공백 데이터
				sb.append("');");
				
				stmt.execute(sb.toString());
			}
		} catch (IOException e) {
			System.out.println("파일에러 : "  + e.getMessage());
		} catch (ClassNotFoundException e) {
			System.out.println("클래스로딩에러 : " + e.getMessage());
		} catch (SQLException e) {
			System.out.println("에러 : " + e.getMessage());
		}
		
	}
	
	public static Connection getConnection() throws ClassNotFoundException, SQLException {
		Class.forName(Request.driver);
		return DriverManager.getConnection(Request.url, Request.id, Request.password); // url, 아이디, 비번
	}
	
	public static List<Data> openData(String str, String start, String end) {
		
		List<Data> datas = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		
		sb.append("SELECT * FROM `").append(str).append("`");
		if(start !=null && end != null)
			sb.append("WHERE ").append(start).append("<=`date` AND `date` <=").append(end);
		sb.append(";");
		
		try (Connection conn=getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sb.toString());) {

			while(rs.next())
				datas.add(new Data(rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8), rs.getString(9)));
			
		} catch (ClassNotFoundException e) {
			System.out.println("클래스로딩에러");
		} catch (SQLException e) {
			System.out.println("에러 : " + e.getMessage());
		} 
		
		return datas;
	}
	
	public static void outputData(String str, String path) {
		
		StringBuilder sb = new StringBuilder();

		sb.append("SELECT * FROM `").append(str).append("`");
		
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(path, true));
				Connection conn=getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sb.toString());){

			bw.write("측정일시,측정소명,이산화질소농도(ppm),오존농도(ppm),이산화탄소농도(ppm),아황산가스(ppm),미세먼지(㎍/㎥),초미세먼지(㎍/㎥)");
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
			System.out.println("입출력에러" + e.getMessage());
		}catch (ClassNotFoundException e) {
			System.out.println("클래스로딩에러");
		} catch (SQLException e) {
			System.out.println("에러 : " + e.getMessage());
		} 
	}
	
	public static String[] showTables() {
		ArrayList<String> str = new ArrayList<>();
	
		try (Connection conn=getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery("SHOW TABLES;");) {
			while(rs.next()) str.add(rs.getString(1));
		} catch (ClassNotFoundException e) {
			System.out.println("클래스로딩에러");
		} catch (SQLException e) {
			System.out.println("에러 : " + e.getMessage());
		}
		
		return str.toArray(new String[str.size()]);
	}
	
}
