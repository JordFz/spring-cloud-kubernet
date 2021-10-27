package mx.jfc.microservices.core.product.services;

import com.mongodb.DuplicateKeyException;
import mx.jfc.microservices.core.api.core.product.Product;
import mx.jfc.microservices.core.api.core.product.ProductService;
import mx.jfc.microservices.core.api.exceptions.InvalidInputException;
import mx.jfc.microservices.core.api.exceptions.NotFoundException;
import mx.jfc.microservices.core.product.persistence.ProductEntity;
import mx.jfc.microservices.core.product.persistence.ProductRepository;
import mx.jfc.microservices.core.util.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductServiceImpl implements ProductService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ServiceUtil serviceUtil;
    private final ProductRepository repository;
    private final ProductMapper mapper;

    @Autowired
    public ProductServiceImpl(ServiceUtil serviceUtil, ProductRepository repository, ProductMapper mapper){
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
        this.repository = repository;
    }

    @Override
    public Product createProduct(Product body) {
        try{
            ProductEntity entity = mapper.apiToEntity(body);
            ProductEntity newEntity = repository.save(entity);

            LOG.debug("createProduct: entity created for productId: {}", body.getProductId());
            return mapper.entityToApi(newEntity);
        } catch (DuplicateKeyException dke){
            throw new InvalidInputException("Duplicate key, product id: "+ body.getProductId());
        }

    }

    @Override
    public Product getProduct(int productId) {

        LOG.debug("/product return the found product for productId={}", productId);
        if(productId < 1){
            throw new InvalidInputException("Invalid product id: "+ productId);
        }
        ProductEntity entity = repository.findByProductId(productId)
                .orElseThrow(() -> new NotFoundException("Not product found for productId: " + productId));
        Product response = mapper.entityToApi(entity);
        response.setServiceAddress(serviceUtil.getServiceAddress());

        LOG.debug("getProduct: found productId: {}", response.getProductId());
        return response;
    }

    @Override
    public void deleteProduct(int productId) {
        LOG.debug("deleteProduct: tries to delete an entity with productId: {}", productId);
        repository.findByProductId(productId).ifPresent(e -> repository.delete(e));
    }
}
