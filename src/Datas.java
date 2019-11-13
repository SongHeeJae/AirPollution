import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Data {
	private String[] datas;
	public Data(String date, String place, String nitrogen, String ozone, String carbon, String gas, String dust, String ultraDust) {
		datas = new String[] {date, place, nitrogen, ozone, carbon, gas, dust, ultraDust};
	}
	
	public static void setInfo(String name, String start, String end) {
		
	}
	
	public String getData(int pos) {
		return datas[pos];
	}
	
	public String getDate() {
		return datas[0];
	}
	
	public String getNitrogen () {
		return datas[2];
	}
	
	public String getOzone() {
		return datas[3];
	}
	
	public String getCarbon() {
		return datas[4];
	}
	
	public String getGas() {
		return datas[5];
	}
	
	public String getDust() {
		return datas[6];
	}
	public String getUltraDust() {
		return datas[7];
	}
	
	public String getPlace() {
		return datas[1];
	}
	
	public String[] getDatas() {
		return datas;
	}
}


public class Datas {

	private List<Data> datas;
	private String name; // 테이블명
	private String start;
	private String end;
	public Datas(String name, List<Data> datas, String start, String end) {
		this.name = name;
		this.datas = datas;
		this.start = start;
		this.end = end;
	}
	
	
	public String getName() {
		return name;
	}
	
	public List<Data> getDatas(){
		return datas;
	}
	
	public String getStart() {
		return start;
	}
	
	public String getEnd() {
		return end;
	}
	
}
