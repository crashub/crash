package org.crsh.jcr.command;

import org.crsh.cmdline.spi.Value;

import java.util.Properties;
import java.util.StringTokenizer;

public class InitProperties extends Value {


  public InitProperties(String string) throws NullPointerException {
    super(string);
  }

  public Properties getProperties() {
    Properties props = new Properties();
    StringTokenizer tokenizer = new StringTokenizer(getString(), ";", false);
    while(tokenizer.hasMoreTokens()){
      String token = tokenizer.nextToken();
      if(token.contains("=")) {
        String key = token.substring(0, token.indexOf('='));
        String value = token.substring(token.indexOf('=') + 1, token.length());
        props.put(key, value);
      }
    }
    return props;
  }

}
