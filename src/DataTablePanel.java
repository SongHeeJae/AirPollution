import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class DataTablePanel extends JPanel {

	private DefaultTableModel dtm;
	private JLabel count;
	private Map<String, List<Data>> datas;
	private List<JCheckBox> boxList;
	private boolean chk = true; // üũ�ڽ�������. placeFilter �޼ҵ� ������ ������ϰ���
	private JPanel placesList; // ���� ��� �г�
	private JTable table;
	public DataTablePanel(Map<String, List<Data>> datas) {
		setLayout(new FlowLayout());
		setPreferredSize(new Dimension(1000, 900));
		table = new JTable();
		count = new JLabel();
		boxList = new ArrayList<>();
		this.datas = datas;

		String[] header = {"�����Ͻ�", "�����Ҹ�" ,"�̻�ȭ���ҳ�(ppm)" , "������(ppm)" , "�̻�ȭź�ҳ�(ppm)" , "��Ȳ�갡��(ppm)", "�̼�����(��/��)", "�ʹ̼�����(��/��)"}; 

		dtm = new DefaultTableModel(header, 0) {
			public boolean isCellEditable(int r, int c) {
				if(c == 0 || c == 1) return false;
				return true;
			}
		};
		
		table.setModel(dtm);
		table.setEnabled(false);
		
		TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(dtm);

		sorter.setComparator(0, (x, y)->{
			double xx = x.toString().length() != 0 ? Double.parseDouble(x.toString()) : -1;
			double yy = y.toString().length() != 0 ? Double.parseDouble(y.toString()) : -1;
			if(xx-yy > 0) return 1;
			else if (xx-yy < 0) return -1;
			else return 0;
		});
		
		table.setRowSorter(sorter);
		table.setAutoCreateRowSorter(false);
		
		JScrollPane pane = new JScrollPane(table);
		
		pane.setPreferredSize(new Dimension(1000, 650));
		add(pane);
		add(count);
		

		placesList = new JPanel(); // ������ ���� �Ҽ��ִ� ���
		
		initPlaceList();
		
		JScrollPane pane2 = new JScrollPane(placesList);
		pane2.setPreferredSize(new Dimension(1000, 50));
		add(pane2);
		
		placeFilter();
	}
	
	public Map<String, List<Data>> getDatas() {
		return datas;
	}
	
	public void initPlaceList() {
		
		placesList.removeAll();
		boxList.clear();
		List<String> list = new ArrayList<>(datas.keySet());
		Collections.sort(list);
		ItemListener listener = e-> {
			if(((JCheckBox)e.getItem()).getText().equals("��ü")) {
				chk=false;
				for(JCheckBox box : boxList)
					if(e.getStateChange() == ItemEvent.SELECTED) box.setSelected(true);
					else box.setSelected(false);
				chk=true;
			}
			if(chk) placeFilter();
		};
		
		list.add(0, "��ü");
		for(String place : list) {
			boxList.add(new JCheckBox(place));
			boxList.get(boxList.size()-1).setSelected(true);
			boxList.get(boxList.size()-1).addItemListener(listener);
			placesList.add(boxList.get(boxList.size()-1));
		}
		
		updateCount();
	}
	
	public void updateCount() {
		count.setText("��ȸ�� : " + dtm.getRowCount());
	}
	
	public void placeFilter() {
		dtm.setNumRows(0);
		
		TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(dtm);
		sorter.setComparator(0, ((TableRowSorter<DefaultTableModel>)table.getRowSorter()).getComparator(0));
		for(int i=2; i<8; i++) sorter.setComparator(i, sorter.getComparator(0));
		table.setRowSorter(sorter); // ���� ���¶�� addRow�� �� �����ϸ鼭 ���⶧���� �ӵ� �������� �ʱ�ȭ ����
		
		for(int i = 1; i<boxList.size(); i++)
			if(boxList.get(i).isSelected())
				for(Data d : datas.get(boxList.get(i).getText())) dtm.addRow(d.getDatas());
		
		updateCount();
	}
	
	public JTable getTable() {
		return table;
	}
	
	public DefaultTableModel getTableModel() {
		return dtm;
	}
	
	public int getRealIndex(int index, String place) {
		for (int i=1; i<boxList.size() && !boxList.get(i).getText().equals(place); i++) 
			if(boxList.get(i).isSelected()) 
				index += datas.get(boxList.get(i).getText()).size();
		return index;
	}
}

