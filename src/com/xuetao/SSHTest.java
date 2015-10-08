package com.xuetao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

public class SSHTest {

	private static final String hostname = "192.168.7.28";
	private static final String username = "root";
	private static final String password = "qw365qb666";

	private static Connection conn;

	static {
		conn = new Connection(hostname);
		try {
			conn.connect();
			if (!conn.authenticateWithPassword(username, password)) {
				throw new IllegalAccessError("认证失败");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static List<String> exeShell(String cmd) {

		String msg = "";
		List<String> msgList = new ArrayList<String>();
		Session session = null;
		try {

			String result = null;
			session = conn.openSession();

			session.execCommand(cmd);
			InputStream stdout = new StreamGobbler(session.getStdout());
			BufferedReader stdoutReader = new BufferedReader(
					new InputStreamReader(stdout));

			while (null != (result = stdoutReader.readLine())) {
				msg += result;
				System.out.println(result);
				msgList.add(result);
			}

			if (null == msg || "".equals(msg)) {
				InputStream stderr = new StreamGobbler(session.getStderr());
				BufferedReader stderrReader = new BufferedReader(
						new InputStreamReader(stderr));

				while (null != (result = stderrReader.readLine())) {
					msg += result;
					msgList.add(result);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != session) {
				session.close();
			}
		}

		return msgList;
	}

	public static void main(String args[]) throws Exception {

		String svnSwitch = "";
		if (args.length > 0) {
			svnSwitch = args[0];
		}

		List<String> msg;

		String codeDir = "cd /Data/code_from_svn/trunk;";
		String webDir = "cd /Data/WEB_APP/new_pay_app/ROOT;";

		String jdkPath = "export JAVA_HOME=/opt/java/jdk1.6.0_43;";
		String mvnPath = "export M2_HOME=/usr/local/apache-maven-3.0.5;";

		//设置系统path变量，制定jdk和maven的bin路径，供之后执行jdk和maven命令
		String pathEnv = jdkPath
				+ mvnPath
				+ "export PATH=$JAVA_HOME/bin:$PATH;export PATH=$PATH:$M2_HOME/bin;";

		if (null == svnSwitch || "".equals(svnSwitch)) {

			msg = SSHTest.exeShell(codeDir + "svn info;");
			svnSwitch = msg.get(1).split(" ")[1];
		} else {
			System.out.println("开始执行switch分支");
			msg = SSHTest.exeShell(codeDir + "svn switch svnSwitch;");
			System.out.println("已经切换当前分支为：" + svnSwitch);
		}

		System.out.println(">>>>>当前分支版本：" + svnSwitch);

		System.out.println(">>>>>执行svn更新操作：");
		msg = SSHTest.exeShell(codeDir + "svn up;");
		
		System.out.println(">>>>>设置jdk和maven变量...");
		msg = SSHTest.exeShell(pathEnv + "echo $PATH");

		System.out.println(">>>>>开始执行打包...");
		msg = SSHTest.exeShell(codeDir + pathEnv
				+ "echo $PATH;mvn clean install -DskipTests=true");

	}
}

