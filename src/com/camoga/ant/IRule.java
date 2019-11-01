package com.camoga.ant;

/**
 * 
 *	Interface to specify the rules to be tested
 */
public interface IRule {
	default long nextRule(long current) {
		return current+1;
	}
}
