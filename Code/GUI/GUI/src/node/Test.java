package node;

import java.util.Scanner;

public class Test {

	static NodeGui gui;

	public static void main(String[] args) {
		gui = new NodeGui();
		gui.setVisible(true);

		boolean flag = true;

		Scanner scanner = new Scanner(System.in);
		String fileName;

		do {
			System.out.print("\n\n\n-----------------------\n[1]: add file\n[2]: delete file\n[0]: Quit\n> ");
			String choise = scanner.nextLine();
			switch (choise.charAt(0)) {
			case '1':
				System.out.print("File name: ");
				fileName = scanner.nextLine();
				System.out.print("Local? ");
				String localnes = scanner.nextLine();
				if (localnes.charAt(0) == 'y')
					gui.addFile (fileName,true);
				else
					gui.addFile (fileName,false);
				break;
			case '2':
				System.out.print("FileName: ");
				fileName = scanner.nextLine();
				gui.deleteFile(fileName);
				break;
			case '0':
				flag = false;
				break;
			default:
				System.out.print("\ninvallid answer");
				break;
			}
		} while (flag);
		
		scanner.close();
		System.exit(0);
	}

}
