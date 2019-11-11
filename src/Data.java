public class Data {
	private String[] datas;
	//public static String name = null; // 테이블명
	public Data(String date, String place, String nitrogen, String ozone, String carbon, String gas, String dust, String ultraDust) {
		datas = new String[] {date, place, nitrogen, ozone, carbon, gas, dust, ultraDust};
	}
	
	public String[] getDatas() {
		return datas;
	}
	
	public String getData(int index) {
		return datas[index];
	}
}
