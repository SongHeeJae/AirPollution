import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class GraphDialog extends JDialog {
	
	private GraphPanel[] gp;
	private Map<String, List<Data>> datas;
	private DefaultTableModel dtm;
	private String start; // 조회기간 시작
	private String end; // 조회기간 끝
	public GraphDialog(String graph,  Map<String, List<Data>> datas, String start, String end) {
		setLayout(new FlowLayout());
		setSize(1050, 1000);
		JTable table = new JTable();
		this.datas = datas;
		this.start = start;
		this.end = end;
		
		String[] header = {"측정소명", "이산화질소농도(ppm)", "오존농도(ppm)", "이산화탄소농도(ppm)", "아황산가스(ppm)", "미세먼지(㎍/㎥)", "초미세먼지(㎍/㎥)"}; 
		
		JTabbedPane tab = new JTabbedPane();
		tab.setPreferredSize(new Dimension(1000, 700));
		
		gp = new GraphPanel[6];
		
		if(graph.equals("막대 그래프"))
			for (int i=0; i<gp.length; i++) gp[i] = new BarGraphPanel(start, end, i);
		else 
			for (int i=0; i<gp.length; i++) gp[i] = new LineGraphPanel(start, end, i);
		
		for(int i=0; i<gp.length; i++) tab.addTab(header[i+1], new JScrollPane(gp[i]));

		add(tab);
		
		if(gp[0] instanceof BarGraphPanel) { // 막대그래프면 정렬버튼 추가
			JButton order = new JButton("정렬");
			order.addActionListener(e-> {
				String text = ((JButton)e.getSource()).getText().equals("+") ? "-" : "+";
				for(int i=0; i<gp.length; i++) gp[i].orderGraph(text);
				order.setText(text);
			});
			add(order);
		}
		
		JLabel duration = new JLabel("조회기간 : " + start + " ~ " + end);
		add(duration);
		JTextField searchText = new JTextField(20);
		add(searchText);
		
		
		JButton dateSearch = new JButton("검색");
		dateSearch.addActionListener(e -> {
			if(datas.get(searchText.getText()) == null) { // 검색 결과 있는지 확인
				JOptionPane.showMessageDialog(null, "검색 결과가 없습니다.");
				return;
			}
			for(int i=0; i<dtm.getRowCount(); i++) // 검색된 지역인지 확인
				if(dtm.getValueAt(i, 0).equals(searchText.getText())) {
					JOptionPane.showMessageDialog(null, "이미 검색된 지역입니다.");
					return;
				} 
			if(gp[0] instanceof LineGraphPanel) addLineGraph(searchText.getText());
			else addBarGraph(searchText.getText());
		});
		add(dateSearch);
		
		dtm = new DefaultTableModel(header, 0) {
			public boolean isCellEditable(int row, int column) { return false; }
		};
		
		table.setModel(dtm);
		JScrollPane pane = new JScrollPane(table);
		table.addKeyListener(new KeyListener() { // 테이블에 키 리스너 등록
			@Override
			public void keyPressed(KeyEvent arg0) { // Del키 누르면 선택된 테이블 삭제
				if(arg0.getKeyCode() == KeyEvent.VK_DELETE && table.getSelectedRow() != -1) {
					for(GraphPanel g : gp) g.removeGraph(table.getSelectedRow());
					dtm.removeRow(table.getSelectedRow());
				}
			}
			
			@Override
			public void keyReleased(KeyEvent arg0) { }
			@Override
			public void keyTyped(KeyEvent arg0) { }
		});
		
		pane.setPreferredSize(new Dimension(1000, 200));
	
		add(pane);
		
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setVisible(true);
	}
	
	public void addLineGraph(String place){
		
		if(dtm.getRowCount() > 5) { // 지역 더 추가할 수 있는지 확인
			JOptionPane.showMessageDialog(null, "더 이상 추가할 수 없습니다.");
			return;
		}
		
		double[] avg = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
		int[] count = {0, 0, 0, 0, 0, 0}; // 누락데이터는 계산안함
		double[] max= {0.0, 0.0, 0.0, 0.0, 0.0, 0.0};

		ArrayList<List<Double>> placeDatas = new ArrayList<>(); // 오염물질별 데이터
		
		for(int i=0; i<gp.length; i++) placeDatas.add(new ArrayList<Double>());
			
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			Calendar cal = Calendar.getInstance();
			cal.setTime(sdf.parse(start));
			cal.add(Calendar.DATE, -1);
			String date = sdf.format(cal.getTime());
		
			for(Data d : datas.get(place)) {
				if(!date.equals(d.getDate())) {
					int days = (int)((sdf.parse(d.getDate()).getTime() - sdf.parse(date).getTime()) / (24*60*60*1000));
					while(days-- > 1)// 누락된 기간은 0으로 채워줌
						for(int i=0; i<gp.length;i ++) placeDatas.get(i).add(0.0);
				}
				for(int i=0; i<gp.length; i++)
					if(d.getData(i+2).length() != 0) {
						double val = Double.parseDouble(d.getData(i+2));
						placeDatas.get(i).add(val);
						count[i]++;
						avg[i] += val;
						max[i] = val > max[i] ? val : max[i]; 
					} else placeDatas.get(i).add(0.0);
				date = d.getDate();
			}			
			int days = (int)((sdf.parse(end).getTime() - sdf.parse(date).getTime()) / (24*60*60*1000)); // 뒤에 남은 누락 기간들 0으로 채워줌
			while(placeDatas.get(0).size() <= days)
				for(int i=0; i<gp.length; i++) placeDatas.get(i).add(0.0);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		
		for(int i=0; i<avg.length; i++) // 평균 계산
			avg[i] = avg[i] != 0 ? Math.round(avg[i] / (double)count[i]*1000)/1000.0 : 0;	// double 0으로 나누면 NaN으로뜸

		dtm.addRow(new Object[] {place, avg[0], avg[1], avg[2], avg[3], avg[4], avg[5]});

		for(int i=0; i<gp.length; i++) gp[i].addLineGraph(place, placeDatas.get(i), max[i]);
	}
	
	public void addBarGraph(String place){
		
		double[] avg = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
		int[] count = {0, 0, 0, 0, 0, 0}; // 누락데이터는 계산 안함
		
		for (Data d : datas.get(place))
			for (int i=0; i<gp.length; i++)
				if(d.getData(i+2).length() != 0) {
					count[i]++;
					avg[i] += Double.parseDouble(d.getData(i+2));
				}

		for(int i=0; i<avg.length; i++) // 평균 계산
			avg[i] = avg[i] != 0 ? Math.round(avg[i] / (double)count[i]*1000)/1000.0 : 0;	// double 0으로 나누면 NaN으로뜸

		dtm.addRow(new Object[] {place, avg[0], avg[1], avg[2], avg[3], avg[4], avg[5]});

		for(int i=0; i<gp.length; i++) gp[i].addBarGraph(place, avg[i]);
	}
}
