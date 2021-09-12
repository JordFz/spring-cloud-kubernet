package mx.jfc.microservices.core.review.services;

import mx.jfc.microservices.core.api.core.review.Review;
import mx.jfc.microservices.core.api.core.review.ReviewService;
import mx.jfc.microservices.core.api.exceptions.InvalidInputException;
import mx.jfc.microservices.core.util.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class ReviewServiceImpl implements ReviewService {

    private static final Logger LOG = LoggerFactory.getLogger(ReviewServiceImpl.class);
    private final ServiceUtil serviceUtil;

    @Autowired
    public ReviewServiceImpl(ServiceUtil serviceUtil){
        this.serviceUtil = serviceUtil;
    }

    @Override
    public List<Review> getReviews(int productId) {
        if(productId < 1){
            throw new InvalidInputException("Invalid ProductID " + productId);
        }
        if(productId == 213){
            LOG.debug("No reviews fond for product id: {}", productId);
            return new ArrayList<>();
        }
        List<Review> list = new ArrayList<>();
        list.add(new Review(productId, 1, "Authos 1", "Subject 1", "Content 1", serviceUtil.getServiceAddress()));
        list.add(new Review(productId, 2, "Authos 2", "Subject 2", "Content 2", serviceUtil.getServiceAddress()));
        list.add(new Review(productId, 3, "Authos 3", "Subject 3", "Content 3", serviceUtil.getServiceAddress()));

        LOG.debug("/reviews response size : {}", list.size());
        return list;
    }
}
