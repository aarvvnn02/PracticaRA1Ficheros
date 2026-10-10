package org.educa.dao;

import generated.Producto;
import generated.Productos;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import java.io.File;
import java.util.List;

public class ProductoDAO {

    public List<Producto> getProductos(String fileXml) throws JAXBException {

        JAXBContext context = JAXBContext.newInstance(Productos.class);

        Unmarshaller unmarshaller = context.createUnmarshaller();

        Productos productos =
                (Productos) unmarshaller.unmarshal(new File(fileXml));

        return productos.getProducto();
    }
}