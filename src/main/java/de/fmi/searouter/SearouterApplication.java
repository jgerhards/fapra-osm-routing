package de.fmi.searouter;

import de.fmi.searouter.dijkstragrid.Grid;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class SearouterApplication {

	public static void main(String[] args) {
		try {
			Grid.importFmiFile("exported_grid.fmi");
		} catch (IOException e) {
			e.printStackTrace();
		}

		SpringApplication.run(SearouterApplication.class, args);
	}

}