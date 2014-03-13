package com.solt.media.update;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class UpdateTool extends JFrame {
	public static final String LIB_FOLDER = "mediaplayer_lib";
	public static final int MAX_TRY_REPLACE = 10;
	private ServerSocket ss;
	
	public UpdateTool() {
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	public void run(String[] args) {
		if (args == null) {
			return;
		}
		Path file = null;
		Path targetFolder = null;
		Path installFolder = FileSystems.getDefault().getPath("./");
		Path libFolder = installFolder.resolveSibling(LIB_FOLDER);
        PrintStream stream = null;
		try {
            stream = new PrintStream(new File("updatetool.txt"));
            System.setErr(stream); //This is important, need to direct error stream somewhere
			waitForPlayerShutdown();
			//delete lib/*
			for (File f : libFolder.toFile().listFiles()) {
				f.delete();
			}
			String component = null;
			int errCnt = 0;
			for (int i = 0; i < args.length; ++i) {
				component = args[i];
				file = installFolder.resolve(component);
				targetFolder = component.contains(LIB_FOLDER) ? libFolder
						: installFolder;
				try {
					Files.move(file, targetFolder.resolve(file.getFileName()),
						StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					e.printStackTrace();
					++errCnt;
					if (errCnt <= MAX_TRY_REPLACE) {
						System.err.println("try again");
						Thread.sleep(500);
						--i;
					} else {
						JOptionPane.showMessageDialog(this,
							    "Lỗi ko thể update MediaPlayer:\n" + e.toString(),
							    "update error", JOptionPane.ERROR_MESSAGE);
						System.err.println("update failed");
						System.exit(1);;
					}
				}
			}
			JOptionPane.showMessageDialog(this,
				    "update thành công MediaPlayer",
				    "update info", JOptionPane.INFORMATION_MESSAGE);
			System.err.println("update success");
			Runtime.getRuntime().exec("MediaPlayer.exe");
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (ss != null) {
				try {
					ss.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (stream != null) {
				stream.close();
			}
		}
	}
	
	public static void main(String[] args) {
		UpdateTool updater = new UpdateTool();
		updater.run(args);
	}
	
	private void waitForPlayerShutdown() throws InterruptedException {
		while (true) {
			try {
				ss = new ServerSocket(18080);
				break;
			} catch (IOException e) {
				Thread.sleep(500);
			}
		}
	}
}