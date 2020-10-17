package com.hii;

import com.hii.repository.DataRepositoryTestSuite;
import com.hii.server.ApiServerTestSuite;
import com.hii.server.WsServerTestSuite;
import com.hii.service.ApiServiceTestSuite;
import com.hii.service.BaseServiceTestSuite;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  DataRepositoryTestSuite.class,
  BaseServiceTestSuite.class,
  ApiServiceTestSuite.class,
  ApiServerTestSuite.class,
  WsServerTestSuite.class
})
public class MainTest {

  @BeforeClass
  public static void setupTest() {}
}
