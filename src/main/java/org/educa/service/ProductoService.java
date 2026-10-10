package org.educa.service;

import generated.Producto;
import jakarta.xml.bind.JAXBException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.educa.dao.ProductoDAO;
import org.educa.entity.ProductoEntity;
import org.educa.entity.SummaryEntity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
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

        List<ProductoEntity> productos = readFile(fileXml);

        // Nombre del archivo XML
        File ficheroXml = new File(fileXml);
        String nombreFichero = ficheroXml.getName();

        int posicionPunto = nombreFichero.lastIndexOf(".");

        if (posicionPunto > 0) {
            nombreFichero = nombreFichero.substring(0, posicionPunto);
        }

        // Fecha del nombre del archivo
        int posicionGuion = nombreFichero.indexOf("_");
        String fecha = nombreFichero.substring(posicionGuion + 1);

        // Crear la carpeta
        File carpeta = new File(path);

        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }

        // Crear Excel
        File ficheroExcel = new File(carpeta, "export_" + fecha + ".xlsx");

        Workbook workbook = new XSSFWorkbook();

        Sheet hoja = workbook.createSheet("Productos");


        // Estilo cabecera

        CellStyle estiloCabecera = workbook.createCellStyle();

        estiloCabecera.setAlignment(HorizontalAlignment.CENTER);

        estiloCabecera.setFillForegroundColor(
                IndexedColors.GREY_25_PERCENT.getIndex());

        estiloCabecera.setFillPattern(
                FillPatternType.SOLID_FOREGROUND);

        // Crear letra en negrita
        org.apache.poi.ss.usermodel.Font fuente = workbook.createFont();
        fuente.setBold(true);

        estiloCabecera.setFont(fuente);


        // Cabecera

        Row filaCabecera = hoja.createRow(0);

        String[] nombresColumnas = {
                "Codigo",
                "Número de Serie",
                "Precio",
                "Descuento",
                "Precio Final",
                "Costes Envío",
                "Costes Almacenaje",
                "Beneficio"
        };

        for (int i = 0; i < nombresColumnas.length; i++) {

            Cell celda = filaCabecera.createCell(i);

            celda.setCellValue(nombresColumnas[i]);

            celda.setCellStyle(estiloCabecera);
        }


        // Estilo fila

        CellStyle estiloFila1 = workbook.createCellStyle();

        estiloFila1.setFillForegroundColor(
                IndexedColors.LIGHT_BLUE.getIndex());

        estiloFila1.setFillPattern(
                FillPatternType.SOLID_FOREGROUND);

        estiloFila1.setAlignment(HorizontalAlignment.CENTER);


        CellStyle estiloFila2 = workbook.createCellStyle();

        estiloFila2.setFillForegroundColor(
                IndexedColors.LIGHT_GREEN.getIndex());

        estiloFila2.setFillPattern(
                FillPatternType.SOLID_FOREGROUND);

        estiloFila2.setAlignment(HorizontalAlignment.CENTER);

        // Productos

        int numeroFila = 1;

        for (ProductoEntity producto : productos) {

            Row fila = hoja.createRow(numeroFila);

            CellStyle estilo;

            if (numeroFila % 2 == 0) {
                estilo = estiloFila2;
            } else {
                estilo = estiloFila1;
            }

            // Codigo
            Cell codigo = fila.createCell(0);
            codigo.setCellValue(producto.getProducto().getCodigo());
            codigo.setCellStyle(estilo);


            // Numero de serie
            Cell numeroSerie = fila.createCell(1);
            numeroSerie.setCellValue(producto.getProducto().getNumeroSerie());
            numeroSerie.setCellStyle(estilo);


            // Precio
            Cell precio = fila.createCell(2);
            precio.setCellValue(producto.getProducto().getPrecio().doubleValue());
            precio.setCellStyle(estilo);


            // Descuento
            Cell descuento = fila.createCell(3);
            descuento.setCellValue(
                    producto.getProducto().getDescuento().doubleValue() / 100);
            descuento.setCellStyle(estilo);


            // Precio final
            Cell precioFinal = fila.createCell(4);
            precioFinal.setCellValue(producto.getPrecioFinal().doubleValue());
            precioFinal.setCellStyle(estilo);


            // Costes envio
            Cell envio = fila.createCell(5);
            envio.setCellValue(
                    producto.getProducto().getCostes().getCostesEnvio().doubleValue());
            envio.setCellStyle(estilo);


            // Costes almacenaje
            Cell almacenaje = fila.createCell(6);
            almacenaje.setCellValue(
                    producto.getProducto().getCostes().getCostesAlmacenaje().doubleValue());
            almacenaje.setCellStyle(estilo);


            // Beneficio
            Cell beneficio = fila.createCell(7);
            beneficio.setCellValue(producto.getProfit().doubleValue());
            beneficio.setCellStyle(estilo);


            numeroFila++;
        }


        // Columnas

        for (int i = 0; i < nombresColumnas.length; i++) {
            hoja.autoSizeColumn(i);
        }


        // Guardar

        FileOutputStream salida = new FileOutputStream(ficheroExcel);

        workbook.write(salida);

        salida.close();


    }

}