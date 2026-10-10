package org.educa.service;

import generated.Producto;
import jakarta.xml.bind.JAXBException;
import org.educa.dao.ProductoDAO;
import org.educa.entity.ProductoEntity;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class ProductoService {

    public List<ProductoEntity> readFile(String fileXml) throws JAXBException {

        ProductoDAO productoDAO = new ProductoDAO();

        List<Producto> productos = productoDAO.getProductos(fileXml);

        List<ProductoEntity> listaProductos = new ArrayList<>();

        for (Producto producto : productos) {

            ProductoEntity productoEntity = new ProductoEntity();

            productoEntity.setProducto(producto);

            BigDecimal precioFinal = producto.getPrecio()
                    .subtract(
                            producto.getPrecio()
                                    .multiply(producto.getDescuento())
                                    .divide(BigDecimal.valueOf(100))
                    )
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal coste = producto.getCostes()
                    .getCostesAlmacenaje()
                    .add(producto.getCostes().getCostesEnvio())
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal beneficio = precioFinal
                    .subtract(coste)
                    .setScale(2, RoundingMode.HALF_UP);

            productoEntity.setPrecioFinal(precioFinal);
            productoEntity.setCost(coste);
            productoEntity.setProfit(beneficio);

            listaProductos.add(productoEntity);
        }

        return listaProductos;
    }

    public void exportSummary(String path, String fileXml)
            throws JAXBException, IOException {

        List<ProductoEntity> productos = readFile(fileXml);

        // Crear el fichero XML
        File ficheroXml = new File(fileXml);

        // Sacar nombre fichero
        String nombreFichero = ficheroXml.getName();

        int posicionPunto = nombreFichero.lastIndexOf(".");

        if (posicionPunto > 0) {
            nombreFichero = nombreFichero.substring(0, posicionPunto);
        }

        // Sacar mes y año fichero
        String nombreSinExtension = nombreFichero;

        String fecha = nombreSinExtension.substring(
                nombreSinExtension.indexOf("_") + 1
        );

        // Calcular total
        BigDecimal beneficioTotal = BigDecimal.ZERO;

        for (ProductoEntity producto : productos) {
            beneficioTotal = beneficioTotal.add(producto.getProfit());
        }

        beneficioTotal = beneficioTotal.setScale(2, RoundingMode.HALF_UP);

        // Resultado
        SummaryEntity summary = new SummaryEntity();

        summary.setName(fecha);
        summary.setNumberOfProducts(productos.size());
        summary.setTotalProfit(beneficioTotal);
        summary.setFileAbsolutePath(ficheroXml.getAbsolutePath());
        summary.setFileName(nombreFichero);
        summary.setFileSize(ficheroXml.length());

        File carpeta = new File(path);

        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }

        // Crear el fichero nuevo
        File ficheroResultado = new File(
                carpeta,
                "result_" + fecha + ".txt"
        );

        FileWriter writer = new FileWriter(ficheroResultado);

        writer.write(summary.toPrint());

        writer.close();
    }


    public void exportExcel(String path, String fileXml)
            throws JAXBException, IOException, ParseException {

        // Se hará en el ejercicio 3
    }
}