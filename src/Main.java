import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.stream.Stream;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

public class Main extends JFrame {

	private Datas datas;
	private JTabbedPane tab;
	private SearchInfoPanel sip;
	private JLabel tableName;
	private JButton insertData;
	private JButton deleteData;

	public Main() {
		setTitle("대기오염도 분석 프로그램");
		setSize(1450, 1000);
		setLayout(new FlowLayout());
		createMenu();
		
		tab = new JTabbedPane();
		tab.setPreferredSize(new Dimension(1000, 800));
		tableName = new JLabel("");
		sip = new SearchInfoPanel(this);
	
		add(tab);
		add(new JLabel("열린 파일 : "));
		add(tableName);
		add(sip);
		
		insertData = new JButton("추가");
		deleteData = new JButton("삭제");
		insertData.setVisible(false);
		deleteData.setVisible(false);
		
		insertData.addActionListener(e -> insertButton());
		deleteData.addActionListener(e -> deleteButton());
		
		add(insertData);
		add(deleteData);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	public String getTableName() {
		return tableName.getText();
	}
	
	public Datas getDatas() {
		return datas;
	}
	
	public void setDatas(Datas datas) { // 열기로 가져온 데이터 지정. 초기에는 전체 탭에 담아줌
		sip.init();
		tab.removeAll();
		this.datas = datas;
		List<Data> list = datas.getDatas();
		Map<String, List<Data>> m = new HashMap<>();
		for (Data data : list) {
			if(m.get(data.getPlace()) == null) m.put(data.getPlace(), new ArrayList<>());
			m.get(data.getPlace()).add(data);
		}
		tab.addTab("전체", new DataTablePanel(m));
		((DataTablePanel)tab.getComponentAt(0)).getTable().setEnabled(true);
		((DataTablePanel)tab.getComponentAt(0)).getTableModel().addTableModelListener(e ->update(e.getFirstRow(), e.getColumn()));
	}
	
	public void setTableDatas(String duration) { // 년, 월 라디오버튼 선택했을 때의 이벤트 처리
		
		removeTab();

		List<Data> list = datas.getDatas();
		Map<String, List<Data>> m = new HashMap<>();

		if (!duration.equals("전체") && !list.isEmpty()) {
			int datelen = duration.equals("년") ? 4 : 6;
			for (int i=0; i<list.size() - 1; i++) {
				if(m.get(list.get(i).getPlace()) == null) m.put(list.get(i).getPlace(), new ArrayList<>());
				m.get(list.get(i).getPlace()).add(list.get(i));
				
				if (!(list.get(i).getDate().substring(0, datelen).equals(list.get(i+1).getDate().substring(0, datelen)))) {
					tab.addTab(list.get(i).getDate().substring(0, datelen), new DataTablePanel(m));
					m = new HashMap<>();
				}
			}
			if(m.get(list.get(list.size()-1).getPlace()) == null) m.put(list.get(list.size()-1).getPlace(), new ArrayList<>());
			m.get(list.get(list.size()-1).getPlace()).add(list.get(list.size()-1)); // 누락된 마지막 한번 수행
			tab.addTab(list.get(list.size()-1).getDate().substring(0, datelen), new DataTablePanel(m));
		}
	}
	
	public void createMenu() { // 메뉴 생성
		JMenuBar mb = new JMenuBar();
		
		JMenu[] menus = {new JMenu("파일"), new JMenu("데이터 통계")};
		String[][] items = {
				{"열기", "파일->서버 저장", "서버->파일 저장", "초기화", "종료"},
				{"막대 그래프", "꺾은선 그래프"}
		};

		MenuActionListener listener = new MenuActionListener();
		for (int i=0; i<menus.length; i++) {
			for(int j=0; j < items[i].length; j++) {
				JMenuItem item = new JMenuItem(items[i][j]);
				item.addActionListener(listener);
				menus[i].add(item);
			}
			mb.add(menus[i]);
		}
		
		setJMenuBar(mb);
	}
	
	private class MenuActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {

			switch(e.getActionCommand()) {
				case "열기":
					open(); // DB 서버 데이터 -> 화면의 표로 출력, 또는 csv 파일 열어서 화면의 표로 출력
					break;
				case "파일->서버 저장":
					saveFileToServer(); // 내 컴퓨터 csv 파일 -> DB 서버 저장만 구현
					break;
				case "서버->파일 저장":
					saveServerToFile(); // 파일 -> 서버 저장만 구현
					break;
				case "초기화":
					insertData.setVisible(false);
					deleteData.setVisible(false);
					tableName.setText("");
					tab.removeAll();
					init();
					break;
				case "종료":
					System.exit(0);
					break;
				default: // 막대, 꺾은선 메뉴
					showGraphDialog(e.getActionCommand());
			}
		}
	}
	
	public void showGraphDialog(String graph) {
		if(getTableName().length() != 0 && !datas.getDatas().isEmpty()) new GraphDialog(graph, ((DataTablePanel)tab.getComponentAt(0)).getDatas(), datas.getStart(), datas.getEnd());
		else JOptionPane.showMessageDialog(null, "데이터를 열어주세요.");
	}
	
	public void init() {
		datas.getDatas().clear();
		sip.init();
	}
	
	public void open() {
		
		JDialog dialog = new JDialog();
		dialog.setModal(true);
		dialog.setLayout(new FlowLayout()); // 다이얼로그 생성
		
		Vector<String> tables = new Vector<>();
		for(String s : Request.showTables()) tables.add(s);
		JList list = new JList(tables);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // 리스트 생성
		
		JButton ok = new JButton("확인");
		JButton cancel = new JButton("취소");
		JButton remove = new JButton("삭제");
		
		remove.addActionListener(e -> {
			int i = list.getSelectedIndex();
			if(i == -1) return;
			Request.remove(tables.get(i));
			tables.remove(i);
			list.repaint();
		});
		ok.addActionListener(e -> {
			String value = (String) list.getSelectedValue();
			if(value == null) return;
			dialog.dispose();
			setDatas(Request.openData(value, null, null));
			setTableDatas("전체");
			insertData.setVisible(true);
			deleteData.setVisible(true);
			tableName.setText(datas.getName());
		}); // 확인시 이벤트 처리
		cancel.addActionListener(e -> dialog.dispose()); // 취소시 종료
		
		dialog.add(new JScrollPane(list));
		dialog.add(ok);
		dialog.add(cancel);
		dialog.add(remove);
		dialog.pack();
		dialog.setVisible(true);
	}
	
	public void saveServerToFile() {
		JDialog dialog = new JDialog();
		dialog.setModal(true);
		dialog.setLayout(new FlowLayout()); // 다이얼로그 생성
		
		JList list = new JList(Request.showTables());
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // 리스트 생성
		
		JButton ok = new JButton("확인");
		JButton cancel = new JButton("취소");
		
		ok.addActionListener(e -> {
			FileDialog fd = new FileDialog(this, "열기", FileDialog.SAVE);
			fd.setDirectory(".");
			fd.setVisible(true);
			
			if(fd.getFile() == null) return; // 입력안되었거나, csv파일 아니면 종료
			String path = fd.getDirectory() + fd.getFile();
			if(!getType(path).equals("csv")) {
				JOptionPane.showMessageDialog(null, "csv파일로 저장해주세요.");
				return;
			}
			
			dialog.dispose();
			String value = (String) list.getSelectedValue();
			if(value == null) return;
			Request.outputData(value, path);
		}); // 확인시 이벤트 처리
		cancel.addActionListener(e -> dialog.dispose()); // 취소시 종료
		
		dialog.add(new JScrollPane(list));
		dialog.add(ok);
		dialog.add(cancel);
		dialog.pack();
		dialog.setVisible(true);
	}
	
	public String getType(String path) { // 확장자명 반환
		return path.substring(path.lastIndexOf(".") + 1).toLowerCase();
	}
	
	public void saveFileToServer() { // 저장 메뉴
		FileDialog dialog = new FileDialog(this, "열기", FileDialog.LOAD);
		dialog.setDirectory(".");
		dialog.setVisible(true);
		
		if(dialog.getFile() == null) return; // 파일가져온거없으면 종료
		
		String path = dialog.getDirectory() + dialog.getFile();

		if(!getType(path).equals("csv")) {
			JOptionPane.showMessageDialog(null, "csv 파일이 아닙니다.");
			return;
		}
		
		String name = JOptionPane.showInputDialog("데이터 이름을 지정해주세요.");
		
		Request.inputData(path, name);
	}
	
	public void insertButton() {
		JDialog dialog = new JDialog();
		dialog.setModal(true);
		dialog.setSize(300, 550);
		dialog.setLayout(new FlowLayout());
		
		String[] text = new String[] {"측정일시", "측정소명", "이산화질소농도(ppm)", "오존농도(ppm)", "이산화탄소농도(ppm)", "아황산가스(ppm)", "미세먼지(㎍/㎥)", "초미세먼지(㎍/㎥)"};
		JTextField[] textField = new JTextField[text.length];
		for(int i=0; i<text.length; i++) {
			JLabel lbl = new JLabel(text[i]);
			textField[i] = new JTextField(20);
			dialog.add(lbl);
			dialog.add(textField[i]);
		}
		
		JButton submit = new JButton("확인");
		submit.addActionListener(ee -> {
			int c=0;
			for(JTextField t : textField) c+=t.getText().length();
			if (c == 0) {
				JOptionPane.showMessageDialog(null, "데이터를 입력해주세요.");
				return;
			} else if(textField[0].getText().length() != 8 || !isNum(textField[0].getText())) {
				JOptionPane.showMessageDialog(null, "날짜 형식이 올바르지 않습니다.");
				return;
			} else if (binarySearchDate(((DataTablePanel)tab.getComponentAt(0)).getDatas().get(textField[1].getText()), textField[0].getText(), false) != -1) {
				JOptionPane.showMessageDialog(null, "각 날짜에 해당하는 장소 데이터는 하나만 입력 가능합니다.");
				return;
			} else {
				try {
					Double.parseDouble(textField[2].getText());
					Double.parseDouble(textField[3].getText());
					Double.parseDouble(textField[4].getText());
					Double.parseDouble(textField[5].getText());
					Double.parseDouble(textField[6].getText());
					Double.parseDouble(textField[7].getText());
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, "오염물질 데이터 형식이 올바르지 않습니다.");
					return;
				}
			}
			
			int pk = Request.insertData(new String[] {textField[0].getText(), textField[1].getText(), textField[2].getText(), textField[3].getText(), textField[4].getText(), textField[5].getText(), textField[6].getText(), textField[7].getText()}, datas.getName());
			if(pk != -1) {
				int index = binarySearchDate(datas.getDatas(), textField[0].getText(), true);
				index = textField[0].getText().compareTo(datas.getDatas().get(index).getDate()) > 0 ? index + 1 : index; 

				datas.getDatas().add(index, new Data(pk, textField[0].getText(), textField[1].getText(), textField[2].getText(), textField[3].getText(), textField[4].getText(), textField[5].getText(), textField[6].getText(), textField[7].getText()));
				
				datas.setStart(datas.getDatas().get(0).getDate());
				datas.setEnd(datas.getDatas().get(datas.getDatas().size()-1).getDate());
				setDatas(datas);
			} else JOptionPane.showMessageDialog(null, "데이터 추가 실패");
		});

		dialog.add(submit);
		
		dialog.setVisible(true);
	}
	
	public void deleteButton() {
		
		DataTablePanel t = (DataTablePanel)tab.getSelectedComponent();
		
		if (tab.getSelectedIndex() != 0 ) {
			JOptionPane.showMessageDialog(null, "삭제는 전체 탭에서 진행 가능합니다.");
			return;
		} else if (t.getTable().getSelectedRow() == -1) {
			JOptionPane.showMessageDialog(null, "선택된 데이터가 없습니다.");
			return;
		}

		int pk = -1;
		
		String place = (String)t.getTable().getValueAt(t.getTable().getSelectedRow(), 1);
		String date = (String)t.getTable().getValueAt(t.getTable().getSelectedRow(), 0);
		
		List<Data> d = t.getDatas().get(place);
		int index = binarySearchDate(d, date, false); // 탭의 데이터에서 삭제
		if(index == -1) return;
		pk = d.get(index).getId();
		d.remove(index);

		t.getTableModel().removeRow(t.getRealIndex(index, place));
		t.updateCount();
		
		index = binarySearchDate(datas.getDatas(), date, false);
		
		if(index == -1) return;
		
		int l, r; // 왼쪽으로 감소, 오른쪽으로 증가 변수. 본래 데이터에서 삭제하기 위해 pk 찾음

		for (l=index; l>0 && datas.getDatas().get(l).getId() != pk && datas.getDatas().get(l).getDate().compareTo(date) <= 0; l--);

		if (datas.getDatas().get(l).getId() == pk) index = l;
		else {
			for(r=index+1; r < datas.getDatas().size() && datas.getDatas().get(r).getId() != pk && datas.getDatas().get(r).getDate().compareTo(date) >= 0; r++);
			index = r;
		}
		
		datas.getDatas().remove(index);
		
		if(!datas.getDatas().isEmpty()) { // 기간의 시작날짜, 끝날짜 재조정
			datas.setStart(datas.getDatas().get(0).getDate());
			datas.setEnd(datas.getDatas().get(datas.getDatas().size()-1).getDate());
		}
		Request.deleteData(pk, datas.getName());
		removeTab();
		sip.init();
	}
	
	public void update(int row, int col) {
		
		DataTablePanel t = (DataTablePanel)tab.getComponentAt(0);
		
		if(col == -1) return;
		else
			try {
				Double.parseDouble(((String)t.getTableModel().getValueAt(row, col)));
			} catch(Exception e) {
				JOptionPane.showMessageDialog(null, "올바른 숫자 데이터를 입력해주세요. 잘못된 데이터는 저장되지 않습니다.");
				return;
			}

		List<Data> d = t.getDatas().get(t.getTableModel().getValueAt(row, 1));
		int index = binarySearchDate(d, (String)t.getTableModel().getValueAt(row, 0), false);
		
		if(index == -1) return;
		int pk = d.get(index).getId();
		String val = (String)t.getTableModel().getValueAt(row, col);
		d.get(index).setData(col, val);
		Request.updateData(pk, datas.getName(), val, col);
		removeTab();
		sip.init();
	}
	
	public int binarySearchDate(List<Data> d, String date, boolean flag) {
		//flag가 false면 실패 시 -1 반환, true면 실패 시 그 위치 반환.
		if(d == null || d.isEmpty()) return -1;
		
		int l = 0;
		int r = d.size() - 1;
		int mid = -1;
		while(l<=r) { 
			mid = (l+r)/2;
			if(d.get(mid).getDate().compareTo(date) < 0) l = mid+1;
			else if(d.get(mid).getDate().compareTo(date) > 0) r = mid-1;
			else return mid;
		}
		return flag ? mid : -1;
	}
	
	public boolean isNum(String text) {
		for (char c : text.toCharArray())
			if('0' > c || c > '9') return false;
		return true;
	}
	
	public void removeTab() { // 전체 탭 제외하고 삭제
		for(int i=tab.getTabCount() -1 ; i>0; i--) tab.remove(i);
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try { Request.getConnection(); } catch (Exception e) {} // 로컬이라그런지 첫 접속이 느려서 실행전에 접속한번하겠습니다
		new Main();
	}

}
