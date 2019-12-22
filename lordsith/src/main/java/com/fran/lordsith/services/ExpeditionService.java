package com.fran.lordsith.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class ExpeditionService {

	@Autowired @Lazy
	private FirefoxClient firefox;
	
	
	
	/**
	Top 1 < 100k points,  1 Destroyer, 1 probes and 33 Larges Cargo
    Top 1 < 1M points 1 Destroyer, 1 probes and 91 Larges Cargo/273 Smalls Cargo
    Top 1 < 5M points 1 Destroyer, 1 probes and 141 Larges Cargo/423 Smalls Cargo
    Top 1 < 25M points 1 Destroyer, 1 probes and 191 Larges Cargo/573 Smalls Cargo
    Top 1 < 50M points 1 Destroyer, 1 probes and 241 Larges Cargo/723 Smalls Cargo
    Top 1 < 75M points  1 Destroyer, 1 probes and 291 Larges Cargo/873 Smalls Cargo
    Top 1 < 100M points  1 Destroyer, 1 probes and 341 Larges Cargo/1.023 Smalls Cargo
    Top 1 > 100M points 1 Destroyer, 1 probes and 417 Larges Cargo/1.223 Smalls Cargo 
	 */
	
}
