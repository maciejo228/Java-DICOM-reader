module com.example.dicom_1 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.dicom_1 to javafx.fxml;
    exports com.example.dicom_1;
}