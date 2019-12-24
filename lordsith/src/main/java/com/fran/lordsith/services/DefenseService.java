package com.fran.lordsith.services;

import org.springframework.stereotype.Service;

@Service
public class DefenseService {
	/*
	 * 960 Misils
	 * 960 Small lasers
	 * 96 Big lassers
	 * 96 Ionics
	 * 48 Gauss
	 * 24 Plasmas
	 * 
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
