package com.rubix.pension.financial_operations.service;

import com.rubix.pension.financial_operations.dto.CreateUnitPriceRequest;
import com.rubix.pension.financial_operations.entity.UnitPrice;
import com.rubix.pension.financial_operations.exception.AllFieldsRequiredException;
import com.rubix.pension.financial_operations.exception.UnitPriceAlreadyUpdatedOnThatDateException;
import com.rubix.pension.financial_operations.repository.UnitPriceRepository;
import com.rubix.pension.userAuth.entity.User;
import com.rubix.pension.userAuth.exception.UserNotFoundException;
import com.rubix.pension.userAuth.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class UnitPriceService {

    private final UnitPriceRepository unitPriceRepository;
    private final UserRepository userRepository;

    public UnitPriceService(UnitPriceRepository unitPriceRepository, UserRepository userRepository){
        this.unitPriceRepository=unitPriceRepository;
        this.userRepository=userRepository;
    }

    public List<UnitPrice> addUnitPrice(Integer userId,CreateUnitPriceRequest request){
        validateRequet(request);
        OffsetDateTime priceDate=request.getPriceDate();

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException("User Not Found")
                );

        if(priceExists(priceDate)){
            throw new UnitPriceAlreadyUpdatedOnThatDateException("The Unit Price is already updated on that day");
        }

        List<UnitPrice> prices= new ArrayList<>();

        if(priceDate.getDayOfWeek()== DayOfWeek.THURSDAY){
            prices.add(createUnitPrice(request,priceDate,user));

            prices.add(createUnitPrice(request,priceDate.plusDays(1),user));

            prices.add(createUnitPrice(request,priceDate.plusDays(2),user));

        }else {
            prices.add(
                    createUnitPrice(
                            request,
                            priceDate,
                            user
                    )
            );
        }

        return unitPriceRepository.saveAll(prices);
    }


    private UnitPrice createUnitPrice(CreateUnitPriceRequest request, OffsetDateTime priceDate,User user){



        UnitPrice unitPrice=new UnitPrice();


        unitPrice.setEntryDate(OffsetDateTime.now());
        unitPrice.setPriceDate(priceDate);

        unitPrice.setFund1(request.getFund1());
        unitPrice.setFund2(request.getFund2());
        unitPrice.setFund3(request.getFund3());
        unitPrice.setFund4(request.getFund4());
        unitPrice.setFund5(request.getFund5());
        unitPrice.setFund6(request.getFund6());
        unitPrice.setFund7(request.getFund7());
        unitPrice.setFund8(request.getFund8());
        unitPrice.setFund9(request.getFund9());
        unitPrice.setFund10(request.getFund10());

        unitPrice.setUserName(user.getUserLogin());

        return unitPrice;
    }

    private boolean priceExists(OffsetDateTime priceDate){
        OffsetDateTime start =
                priceDate
                        .toLocalDate()
                        .atStartOfDay(
                                priceDate.getOffset()
                        ).toOffsetDateTime();

        OffsetDateTime end = start.plusDays(1);

        return unitPriceRepository.countByPriceDate(start,end)>0;
    }


    private void validateRequet(CreateUnitPriceRequest request){
        if (request.getPriceDate() == null ||
                request.getFund1() == null ||
                request.getFund2() == null ||
                request.getFund3() == null ||
                request.getFund4() == null ||
                request.getFund5() == null ||
                request.getFund6() == null ||
                request.getFund7() == null ||
                request.getFund8() == null ||
                request.getFund9() == null ||
                request.getFund10() == null) {

            throw new AllFieldsRequiredException(
                    "All fields are required."
            );
        }
    }



}
