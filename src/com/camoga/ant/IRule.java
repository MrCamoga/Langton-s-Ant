package com.camoga.ant;

/**
 * 
 *	Interface to specify the rules to be tested
 */
public interface IRule {
	public long nextRule(long current);
}
