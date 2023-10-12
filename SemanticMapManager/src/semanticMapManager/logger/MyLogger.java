package semanticMapManager.logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyLogger {
	private static final String LOG_FILE_PREFIX = "Log_";
	private String logFileName;

    public MyLogger(String logType) {
        // 생성자에서 로그 파일 이름 생성
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        logFileName = "log" + File.separator + logType +LOG_FILE_PREFIX + dateFormat.format(new Date()) + ".txt";
    }

    public void log(String message) {
        try {
            // 로그 파일 열기
            FileWriter fileWriter = new FileWriter(logFileName, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            PrintWriter printWriter = new PrintWriter(bufferedWriter);

            // 로그 메시지 작성
            String logMessage = getCurrentTimeStamp() + " - " + message;

            // 로그 파일에 로그 추가
            printWriter.println(logMessage);

            // 파일 닫기
            printWriter.close();
//            System.out.println("로그가 성공적으로 기록되었습니다.");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("로그 파일을 열거나 작성하는 동안 오류가 발생했습니다.");
        }
    }

    // 현재 시간을 포맷팅하는 메서드
    private static String getCurrentTimeStamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(new Date());
    }

    public static void main(String[] args) {
        MyLogger logger = new MyLogger("test");

        // 로그 메시지 기록
        logger.log("This is a sample log message 1.");
        logger.log("This is a sample log message 2.");
    }
}
