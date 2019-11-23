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

import javax.swing.DefaultListModel;
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
import javax.swing.ListSelectionModel;

public class Main extends JFrame {

	private Datas datas;
	private JTabbedPane tab;
	private SearchInfoPanel sip;
	private JLabel tableName;
	public Main() {
		setTitle("대기오염도 분석 프로그램");
		setSize(1500, 1000);
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
		tab.removeAll();
		this.datas = datas;
		List<Data> list = datas.getDatas();
		Map<String, List<Data>> m = new HashMap<>();
		for (Data data : list) {
			if(m.get(data.getPlace()) == null) m.put(data.getPlace(), new ArrayList<>());
			m.get(data.getPlace()).add(data);
		}
		tab.addTab("전체", new DataTablePanel(m));
	}
	
	public void setTableDatas(String duration) { // 년, 월 라디오버튼 선택했을 때의 이벤트 처리
		
		for(int i=tab.getTabCount() -1 ; i>0; i--) tab.remove(i);

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
			sip.init();
			setTableDatas("전체");
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
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new Main();
	}

}
