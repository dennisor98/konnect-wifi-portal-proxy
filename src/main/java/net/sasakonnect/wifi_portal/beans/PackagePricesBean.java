package net.sasakonnect.wifi_portal.beans;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import net.sasakonnect.wifi_portal.domain.InternetPackages;
import net.sasakonnect.wifi_portal.repository.InternetPackageRepository;

@Component
public class PackagePricesBean {
	@Autowired
	InternetPackageRepository packagesRepository;
	public List<String> packagePrices =  new ArrayList<>();
    @PostConstruct
    private void getPackagePrices() {
    	List<InternetPackages> packages = packagesRepository.findAll();
    	packages.stream().map(p->{
    		return packagePrices.add(String.valueOf(p.getCost()));
    		
    	}).collect(Collectors.toList());
    }
    
}
