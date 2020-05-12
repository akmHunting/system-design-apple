package test;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import main.SystemDesign;

public class SystemDesignTest {

	public static void main(String[] args) {
		
		testDependOneLine();
		testDependTwoLine();
		
		// need to write more testcases.
		
	}
	
	
	static public void testDependOneLine() {
		SystemDesign sd = new SystemDesign();
		byte[] bytes = new StringBuffer("DEPEND TELNET TCPIP NETCARD\nEND").toString().getBytes();
		InputStream inputStream = new ByteArrayInputStream(bytes);
		boolean retval = sd.parseAndRun(inputStream);
		assert(retval ==Boolean.TRUE);
	}

	static public void testDependTwoLine() {
		SystemDesign sd = new SystemDesign();
		byte[] bytes = new StringBuffer("DEPEND TELNET TCPIP NETCARD\nDEPEND TCPIP NETCARD\nEND").toString().getBytes();
		InputStream inputStream = new ByteArrayInputStream(bytes);
		boolean retval = sd.parseAndRun(inputStream);
		assert(retval ==Boolean.TRUE);
	}

	
}


