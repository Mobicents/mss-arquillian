package org.jboss.arquillian.container.mobicents.servlet.sip.tomcat.embedded_6;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author <a href="mailto:gvagenas@gmail.com">George Vagenas</a>
 */
@RunWith(Suite.class)
@SuiteClasses({
	MyTest1.class,
	MyTest2.class
})
public class Testsuite {

	//Issue 4: http://code.google.com/p/commtesting/issues/detail?id=4
	//Issue 10: http://code.google.com/p/commtesting/issues/detail?id=10
}
