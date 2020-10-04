package com.cars24.biddingsystem;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.cars24.biddingsystem.constants.AuctionStatus;
import com.cars24.biddingsystem.constants.ERole;
import com.cars24.biddingsystem.jpa.model.Auction;
import com.cars24.biddingsystem.jpa.model.Role;
import com.cars24.biddingsystem.jpa.model.User;
import com.cars24.biddingsystem.repository.AuctionRepository;
import com.cars24.biddingsystem.repository.UserRepository;

@SpringBootApplication
public class BiddingSystemApplication {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private AuctionRepository auctionRepository;

	public static void main(String[] args) {
		SpringApplication.run(BiddingSystemApplication.class, args);
	}

	@Bean
	InitializingBean sendDatabase() {
		return () -> {
			userRepository.save(new User("Divakar", "kdivakar.diva1992@gmail.com",
					passwordEncoder.encode("cars24-bidding-system"), new Role(ERole.ROLE_ADMIN)));
			userRepository.save(new User("Shiv", "shiv.mohan.tiwari@cars24.com",
					passwordEncoder.encode("cars24-bidding-system"), new Role(ERole.ROLE_USER)));
			userRepository.save(new User("Mozammil", "mozammil.afroz@cars24.com",
					passwordEncoder.encode("cars24-bidding-system"), new Role(ERole.ROLE_MODERATOR)));

			auctionRepository.save(new Auction("Chair", 1000.0f, 10.0f, AuctionStatus.RUNNING));
			auctionRepository.save(new Auction("Table", 5000.0f, 50.0f, AuctionStatus.OVER));
			auctionRepository.save(new Auction("Cot", 10000.0f, 100.0f, AuctionStatus.OVER));
			auctionRepository.save(new Auction("Car", 50000.0f, 500.0f, AuctionStatus.RUNNING));
			auctionRepository.save(new Auction("Bus", 100000.0f, 1000.0f, AuctionStatus.RUNNING));
			auctionRepository.save(new Auction("Bike", 8000.0f, 80.0f, AuctionStatus.RUNNING));
			auctionRepository.save(new Auction("Mobile oppo", 10000.0f, 100.0f, AuctionStatus.RUNNING));
			auctionRepository.save(new Auction("Computor", 35000.0f, 35.0f, AuctionStatus.RUNNING));
			auctionRepository.save(new Auction("Key board", 800.0f, 8.0f, AuctionStatus.YET_TO_START));
			auctionRepository.save(new Auction("Cattle", 9900.0f, 9.0f, AuctionStatus.RUNNING));

		};
	}

}
