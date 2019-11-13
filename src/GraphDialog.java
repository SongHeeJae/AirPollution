import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
	private String start;
	private String end;
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
		
		JLabel duration = new JLabel(start + " ~ " + end);
		add(duration);
		JTextField searchText = new JTextField(20);
		add(searchText);
		
		
		JButton dateSearch = new JButton("검색");
		dateSearch.addActionListener(e -> addTableRow(searchText.getText()));
		add(dateSearch);
		
		dtm = new DefaultTableModel(header, 0) {
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		
		table.setModel(dtm);
		JScrollPane pane = new JScrollPane(table);
		table.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent arg0) {
				if(arg0.getKeyCode() == KeyEvent.VK_DELETE && table.getSelectedRow() != -1) {
					for(GraphPanel g : gp) g.removeGraph(table.getSelectedRow());
					dtm.removeRow(table.getSelectedRow());
				}
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
			}

			@Override
			public void keyTyped(KeyEvent arg0) {
			}
			
		});
		
		pane.setPreferredSize(new Dimension(1000, 200));
	
		add(pane);
		
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setVisible(true);
	}
	
	public void addTableRow(String place) {
		
		// 개선해야할부분 너무 비효율적
		
		if(gp[0] instanceof LineGraphPanel && dtm.getRowCount() > 5) { // 지역 더 추가할 수 있는지 확인
			JOptionPane.showMessageDialog(null, "더 이상 추가할 수 없습니다.");
			return;
		}
		
		if(datas.get(place) == null) { // 검색 결과 있는지 확인
			JOptionPane.showMessageDialog(null, "검색 결과가 없습니다.");
			return;
		}
		
		for(int i=0; i<dtm.getRowCount(); i++) // 검색된 지역인지 확인
			if(dtm.getValueAt(i, 0).equals(place)) {
				JOptionPane.showMessageDialog(null, "이미 검색된 지역입니다.");
				return;
			} 
		
		double[] avg = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
		int[] count = {0, 0, 0, 0, 0, 0};
		
		List<Data> data = datas.get(place);
		
		ArrayList<List<Double>> placeDatas = new ArrayList<>();
		for(int i=0; i<gp.length; i++) placeDatas.add(new ArrayList<Double>());
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String date = start;
		for(Data d : data) {
			try {
				int days = (int)((sdf.parse(d.getDate()).getTime() - sdf.parse(date).getTime()) / (24*60*60*1000));
				while(days-- > 1) // 누락된 기간때문에 0으로 채워줌
					for(int i=0; i<gp.length;i ++) placeDatas.get(i).add(0.0);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			for(int i=0; i<gp.length; i++) {
				if(d.getData(i+2).length() != 0) {
					placeDatas.get(i).add(Double.parseDouble(d.getData(i+2)));
					count[i]++;
					avg[i] += Double.parseDouble(d.getData(i+2));
				} else placeDatas.get(i).add(0.0);
			}
			date = d.getDate();
			
		}
		try { // 뒤에 남은 누락 기간들 0으로 채워줌
			int days = (int)((sdf.parse(end).getTime() - sdf.parse(date).getTime()) / (24*60*60*1000));
			while(placeDatas.get(0).size() <= days)
				for(int i=0; i<gp.length; i++) placeDatas.get(i).add(0.0);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		for(int i=0; i<avg.length; i++) // 평균 계산
			avg[i] = avg[i] != 0 ? avg[i] / (double)count[i] : 0;	// double 0으로 나누면 NaN으로뜸

		String[] row = {place, String.format("%.3f", avg[0]), String.format("%.3f", avg[1]), String.format("%.3f", avg[2]), String.format("%.3f", avg[3]), String.format("%.3f", avg[4]), String.format("%.3f", avg[5])};

		dtm.addRow(row);
		
		if(gp[0] instanceof BarGraphPanel)
			for(int i=0; i<gp.length; i++) gp[i].addGraph(place, Double.parseDouble(row[i+1]));
		else 
			for(int i=0; i<gp.length; i++) gp[i].addGraph(place, placeDatas.get(i));
	}
}
