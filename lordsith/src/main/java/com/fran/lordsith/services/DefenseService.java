package com.fran.lordsith.services;

import org.springframework.stereotype.Service;

@Service
public class DefenseService {
	/*
	 * 1920 Misils
	 * 1920 Small lasers
	 * 192 Big lassers
	 * 192 Ionics
	 * 96 Gauss
	 * 48 Plasmas
	 */
	
	public long calculateNumberOfDefense(int baseNumber, long points) {
		if(points < 50000) return baseNumber * 0;
		if(points < 500000) return Double.valueOf(baseNumber * 0.25).longValue();
		if(points < 1000000) return Double.valueOf(baseNumber * 0.50).longValue();
		if(points > 1000000) {
			return (points/1000000) * baseNumber;
		}
		
		return 0;
	}
}
