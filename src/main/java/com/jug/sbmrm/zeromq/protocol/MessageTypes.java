/**
 * 
 */
package com.jug.sbmrm.zeromq.protocol;

public interface MessageTypes {

	Class< ? > classForId(int id);

	int idForClass(Class<?> klass);
}