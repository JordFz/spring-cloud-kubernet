package mx.jfc.microservices.core.product.services;

import mx.jfc.microservices.core.api.core.product.Product;
import mx.jfc.microservices.core.api.core.product.ProductService;
import mx.jfc.microservices.core.api.exceptions.InvalidInputException;
import mx.jfc.microservices.core.api.exceptions.NotFoundException;
import mx.jfc.microservices.core.util.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductServiceImpl implements ProductService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ServiceUtil serviceUtil;

    @Autowired
    public ProductServiceImpl(ServiceUtil serviceUtil){
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Product getProduct(int productId) {

        LOG.debug("/product return the found product for productId={}", productId);
        if(productId < 1){
            throw new InvalidInputException("Invalid product id: "+ productId);
        }
        if(productId == 13){
            throw new NotFoundException("No product found for productId: " + productId);
        }

        return new Product(productId, "name -" + productId,123, serviceUtil.getServiceAddress());
    }
}
