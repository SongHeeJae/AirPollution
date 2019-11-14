import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class DataTablePanel extends JPanel {

	private DefaultTableModel dtm;
	private JLabel count;
	private Map<String, List<Data>> datas;
	private List<JCheckBox> boxList;
	private boolean chk = true; // üũ�ڽ�������. placeFilter �޼ҵ� ������ ������ϰ���

	public DataTablePanel(Map<String, List<Data>> datas) {
		setLayout(new FlowLayout());
		setPreferredSize(new Dimension(1000, 900));
		JTable table = new JTable();
		count = new JLabel();
		boxList = new ArrayList<>();
		this.datas = datas;

		String[] header = {"�����Ͻ�", "�����Ҹ�" ,"�̻�ȭ���ҳ�(ppm)" , "������(ppm)" , "�̻�ȭź�ҳ�(ppm)" , "��Ȳ�갡��(ppm)", "�̼�����(��/��)", "�ʹ̼�����(��/��)"}; 

		dtm = new DefaultTableModel(header, 0) {
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		table.setModel(dtm);
		
		
		// �����Һκ�
		table.setAutoCreateRowSorter(true);
		
		JScrollPane pane = new JScrollPane(table);
		
		pane.setPreferredSize(new Dimension(1000, 650));
		add(pane);
		add(count);
		
		initPlaceList();
		
		placeFilter();
	}
	
	public Map<String, List<Data>> getDatas() {
		return datas;
	}
	
	public void initPlaceList() {
		JPanel placesList = new JPanel(); // ������ ���� �Ҽ��ִ� ���
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
		JScrollPane pane = new JScrollPane(placesList);
		pane.setPreferredSize(new Dimension(1000, 50));
		add(pane);
		
		count.setText("��ȸ�� : " + dtm.getRowCount());
	}
	
	public void placeFilter() {
		dtm.setNumRows(0);
		for(int i = 1; i<boxList.size(); i++)
			if(boxList.get(i).isSelected())
				for(Data d : datas.get(boxList.get(i).getText())) dtm.addRow(d.getDatas());
		count.setText("��ȸ�� : " + dtm.getRowCount());
	}
}

