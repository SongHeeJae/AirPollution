import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
	
	public void setDatas(Datas datas) {
		this.datas = datas;
	}
	
	public void setTableDatas(String duration) {
		
		tab.removeAll();
		
		List<Data> list = new ArrayList<>();
		for (String s : this.datas.getDatas().keySet())
			list.addAll(this.datas.getDatas().get(s));
		list.sort((x, y)->x.getDate().compareTo(y.getDate()));
		datas.setStart(list.get(0).getDate());
		datas.setEnd(list.get(list.size()-1).getDate());
		
		DataTablePanel dtp = new DataTablePanel();
		if(duration.equals("전체")) {
			tab.addTab("전체", dtp);
			for (Data data : list) dtp.add(data);
		} else {

			int datelen = duration.equals("년") ? 4 : 6; // 
			
			tab.addTab(list.get(0).getDate().substring(0, datelen), dtp);
			for (Data data : list) {
				if (!(data.getDate().substring(0, datelen).equals(tab.getTitleAt(tab.getTabCount() - 1)))) {
					dtp.initPlaceList();
					dtp = new DataTablePanel();
					tab.addTab(data.getDate().substring(0, datelen), dtp);
				}
				dtp.add(data);
			}
		}
		dtp.initPlaceList();
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
					break;
			}
		}
	}
	
	public void showGraphDialog(String graph) {
		if(getTableName().length() != 0) new GraphDialog(graph, datas);
		else JOptionPane.showMessageDialog(null, "데이터를 열어주세요.");
	}
	
	public void init() {
		datas.getDatas().clear();
		sip.init();
	}
	
	public void open() {
		
		// 열린 파일 있을경우 별도 예외처리
		
		JDialog dialog = new JDialog();
		dialog.setModal(true);
		dialog.setLayout(new FlowLayout()); // 다이얼로그 생성
		
		JList list = new JList(Request.showTables());
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // 리스트 생성
		
		JButton ok = new JButton("확인");
		JButton cancel = new JButton("취소");
		ok.addActionListener(e -> {
			
			String value = (String) list.getSelectedValue();
			if(value == null) return;
			dialog.dispose();
			setDatas(Request.openData(value, null, null));
			setTableDatas("전체");
			tableName.setText(datas.getName());
		}); // 확인시 이벤트 처리
		cancel.addActionListener(e -> dialog.dispose()); // 취소시 종료
		
		dialog.add(new JScrollPane(list));
		dialog.add(ok);
		dialog.add(cancel);
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
