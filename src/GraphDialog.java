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
	private String start; // ��ȸ�Ⱓ ����
	private String end; // ��ȸ�Ⱓ ��
	public GraphDialog(String graph,  Map<String, List<Data>> datas, String start, String end) {
		setLayout(new FlowLayout());
		setSize(1050, 1000);
		JTable table = new JTable();
		this.datas = datas;
		this.start = start;
		this.end = end;
		
		String[] header = {"�����Ҹ�", "�̻�ȭ���ҳ�(ppm)", "������(ppm)", "�̻�ȭź�ҳ�(ppm)", "��Ȳ�갡��(ppm)", "�̼�����(��/��)", "�ʹ̼�����(��/��)"}; 
		
		JTabbedPane tab = new JTabbedPane();
		tab.setPreferredSize(new Dimension(1000, 700));
		
		gp = new GraphPanel[6];
		
		if(graph.equals("���� �׷���"))
			for (int i=0; i<gp.length; i++) gp[i] = new BarGraphPanel(start, end, i);
		else 
			for (int i=0; i<gp.length; i++) gp[i] = new LineGraphPanel(start, end, i);
		
		for(int i=0; i<gp.length; i++) tab.addTab(header[i+1], new JScrollPane(gp[i]));

		add(tab);
		
		if(gp[0] instanceof BarGraphPanel) { // ����׷����� ���Ĺ�ư �߰�
			JButton order = new JButton("����");
			order.addActionListener(e-> {
				String text = ((JButton)e.getSource()).getText().equals("+") ? "-" : "+";
				for(int i=0; i<gp.length; i++) gp[i].orderGraph(text);
				order.setText(text);
			});
			add(order);
		}
		
		JLabel duration = new JLabel("��ȸ�Ⱓ : " + start + " ~ " + end);
		add(duration);
		JTextField searchText = new JTextField(20);
		add(searchText);
		
		
		JButton dateSearch = new JButton("�˻�");
		dateSearch.addActionListener(e -> {
			if(datas.get(searchText.getText()) == null) { // �˻� ��� �ִ��� Ȯ��
				JOptionPane.showMessageDialog(null, "�˻� ����� �����ϴ�.");
				return;
			}
			for(int i=0; i<dtm.getRowCount(); i++) // �˻��� �������� Ȯ��
				if(dtm.getValueAt(i, 0).equals(searchText.getText())) {
					JOptionPane.showMessageDialog(null, "�̹� �˻��� �����Դϴ�.");
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
		table.addKeyListener(new KeyListener() { // ���̺� Ű ������ ���
			@Override
			public void keyPressed(KeyEvent arg0) { // DelŰ ������ ���õ� ���̺� ����
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
		
		if(dtm.getRowCount() > 5) { // ���� �� �߰��� �� �ִ��� Ȯ��
			JOptionPane.showMessageDialog(null, "�� �̻� �߰��� �� �����ϴ�.");
			return;
		}
		
		double[] avg = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
		int[] count = {0, 0, 0, 0, 0, 0}; // ���������ʹ� ������
		double[] max= {0.0, 0.0, 0.0, 0.0, 0.0, 0.0};

		ArrayList<List<Double>> placeDatas = new ArrayList<>(); // ���������� ������
		
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
					while(days-- > 1)// ������ �Ⱓ�� 0���� ä����
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
			int days = (int)((sdf.parse(end).getTime() - sdf.parse(date).getTime()) / (24*60*60*1000)); // �ڿ� ���� ���� �Ⱓ�� 0���� ä����
			while(placeDatas.get(0).size() <= days)
				for(int i=0; i<gp.length; i++) placeDatas.get(i).add(0.0);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		
		for(int i=0; i<avg.length; i++) // ��� ���
			avg[i] = avg[i] != 0 ? Math.round(avg[i] / (double)count[i]*1000)/1000.0 : 0;	// double 0���� ������ NaN���ζ�

		dtm.addRow(new Object[] {place, avg[0], avg[1], avg[2], avg[3], avg[4], avg[5]});

		for(int i=0; i<gp.length; i++) gp[i].addLineGraph(place, placeDatas.get(i), max[i]);
	}
	
	public void addBarGraph(String place){
		
		double[] avg = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
		int[] count = {0, 0, 0, 0, 0, 0}; // ���������ʹ� ��� ����
		
		for (Data d : datas.get(place))
			for (int i=0; i<gp.length; i++)
				if(d.getData(i+2).length() != 0) {
					count[i]++;
					avg[i] += Double.parseDouble(d.getData(i+2));
				}

		for(int i=0; i<avg.length; i++) // ��� ���
			avg[i] = avg[i] != 0 ? Math.round(avg[i] / (double)count[i]*1000)/1000.0 : 0;	// double 0���� ������ NaN���ζ�

		dtm.addRow(new Object[] {place, avg[0], avg[1], avg[2], avg[3], avg[4], avg[5]});

		for(int i=0; i<gp.length; i++) gp[i].addBarGraph(place, avg[i]);
	}
}
