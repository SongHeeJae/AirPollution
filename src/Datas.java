import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Data {
	private int id;
	private String[] datas;
	public Data(int id, String date, String place, String nitrogen, String ozone, String carbon, String gas, String dust, String ultraDust) {
		this.id = id;
		datas = new String[] {date, place, nitrogen, ozone, carbon, gas, dust, ultraDust};
	}
	
	public String getData(int pos) {
		return datas[pos];
	}
	
	public int getId() {
		return id;
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
	
	public void setData(int pos, String val) {
		datas[pos] = val;
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
	
	public void setStart(String start) {
		this.start = start;
	}
	
	public void setEnd(String end) {
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
