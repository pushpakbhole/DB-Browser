package sample;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.sql.*;
import java.util.Properties;

public class dbBrowserKK {
     @FXML
    TextField tfUsername,tfPassword;
     @FXML
    Button btConnect,btConnectServer;
     @FXML
    ComboBox cbTableNames,cbDatabaseNames;
     @FXML
    TableView tvTable;
     @FXML
    VBox vboxForm;
    private static Connection connectionF;
    private static Connection connection;
    ObservableList<String> tablenames;
    ObservableList<String> databasenames;
    ObservableList<ObservableList> dataForTable;
    int lastindex =-1;
    int firstindex=-1;

    public void initialize(){
        tablenames = FXCollections.observableArrayList();
        databasenames = FXCollections.observableArrayList();
        cbTableNames.getItems().clear();
        cbDatabaseNames.getItems().clear();
        btConnect.setDisable(true);
        cbTableNames.setOnAction(e -> {
            tvTable.getItems().clear();
            tvTable.getColumns().clear();
            try {
                genTable();
            } catch (SQLException ex) {
                System.out.println("burda dert var");
            }


        });


    }
    public  void getConnection() {
        Properties connectionProps = new Properties();
        connectionProps.put("user", tfUsername.getText());
        if(tfPassword.getText().isEmpty()){
            connectionProps.put("password", "");
        }else{
            connectionProps.put("password", tfPassword.getText());
        }

        try {
            Class.forName("com.mysql.jdbc.Driver");
            connectionF = DriverManager.getConnection("jdbc:" + "mysql" + "://" + "127.0.0.1" + ":" + "3306" + "/" + "", connectionProps);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Statement stmt = null;
        ResultSet resultset = null;

        try {
            stmt = connectionF.createStatement();
            resultset = stmt.executeQuery("SHOW DATABASES;");

            if (stmt.execute("SHOW DATABASES;")) {
                resultset = stmt.getResultSet();
            }

            while (resultset.next()) {

                databasenames.add(resultset.getString("Database"));
            }
            cbDatabaseNames.getItems().addAll(databasenames);
            cbDatabaseNames.getSelectionModel().selectFirst();
            btConnect.setDisable(false);
        } catch (SQLException ex) {
            // handle any errors
            ex.printStackTrace();
        } finally {
            // release resources
            if (resultset != null) {
                try {
                    resultset.close();
                } catch (SQLException sqlEx) {
                }
                resultset = null;
            }

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx) {
                }
                stmt = null;
            }

            if (connectionF != null) {
                try {
                    connectionF.close();
                } catch (SQLException sqlEx) {
                }
                connectionF = null;
            }
        }
        btConnectServer.setDisable(true);
    }

    public void connectButtonAction() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");


        connection = DriverManager.getConnection("jdbc:mysql://localhost/"+cbDatabaseNames.getValue().toString(), tfUsername.getText(), tfPassword.getText());

        btConnect.setStyle("-fx-base: green;");
        btConnect.setDisable(true);
        btConnect.setText("Connected to "+ cbDatabaseNames.getValue().toString());

        DatabaseMetaData md = connection.getMetaData();

        ResultSet rs = md.getTables(null, null, "%", null);
        while (rs.next()) {
            tablenames.add(rs.getString(3));
        }
        cbTableNames.getItems().addAll(tablenames);
        cbTableNames.getSelectionModel().selectFirst();




    }
    public void genTable() throws SQLException {
        dataForTable=FXCollections.observableArrayList();
        dataForTable.clear();
        vboxForm.getChildren().clear();
        tvTable.getItems().clear();
        tvTable.getColumns().clear();

        String SQL = "SELECT * from "+cbTableNames.getValue().toString();
        //ResultSet
        ResultSet rs=connection.createStatement().executeQuery(SQL);


        for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
            //We are using non property style for making dynamic table
            final int j = i;
            TableColumn col = new TableColumn(rs.getMetaData().getColumnName(i + 1));
            col.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList, String> param) {
                    if(param.getValue().get(j) == null){
                        return new SimpleStringProperty("Null");
                    }else {
                        return new SimpleStringProperty(param.getValue().get(j).toString());
                    }

                }
            });
            tvTable.getColumns().addAll(col);
        }

        while (rs.next()) {
            //Iterate Row
            ObservableList<String> row = FXCollections.observableArrayList();
            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                //Iterate Column
                row.add(rs.getString(i));
            }
            dataForTable.add(row);


        }

        //FINALLY ADDED TO TableView
        tvTable.setItems(dataForTable);
        tvTable.getSelectionModel().selectLast();
        lastindex= tvTable.getSelectionModel().getSelectedIndex();
        tvTable.getSelectionModel().selectFirst();
        firstindex= tvTable.getSelectionModel().getSelectedIndex();
    }
    public void insertRow() throws SQLException {
       Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM "+cbTableNames.getSelectionModel().getSelectedItem().toString());

        ResultSetMetaData rsMetaData = rs.getMetaData();
        int colCount = rsMetaData.getColumnCount();
        String[] a = new String[colCount];

        // column nameleri bir arraye al for later use
        for (int i = 1; i < colCount + 1; i++) {
            String columnName = rsMetaData.getColumnName(i);
            a[i-1] = columnName;
        }
        vboxForm.getChildren().clear();
        TextField[] tfArray = new TextField[colCount];
        Label info = new Label();

        info.setText("Please fill all the fields before submitting anything.");
        vboxForm.getChildren().add(info);
        for (int i = 0; i < colCount; i++) {
            HBox hbox = new HBox();
            Label lb = new Label();
            lb.setText(a[i]);
            TextField gentf = new TextField();
            tfArray[i] = gentf;
            hbox.getChildren().addAll(lb,gentf);
            vboxForm.getChildren().add(hbox);

        }
        Button btSubmit = new Button();
        btSubmit.setText("Submit");
        btSubmit.setOnAction(e -> {
            String fieldnames="";
            for (int i=0;i<a.length;i++){
                fieldnames=fieldnames+a[i]+", ";
            }
            fieldnames=fieldnames.substring(0, fieldnames.length() - 2);
            String questionmarks=" values (";
            for (int i=0;i<colCount;i++){
                questionmarks=questionmarks+"?, ";
            }
            questionmarks=questionmarks.substring(0,questionmarks.length()-2);
            questionmarks=questionmarks+")";
            String q = "insert into " + cbTableNames.getSelectionModel().getSelectedItem().toString()+ " (" + fieldnames+")"+questionmarks;

            PreparedStatement preparedStmt = null;
            try {
                preparedStmt = connection.prepareStatement(q);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            for (int i=0;i<colCount;i++){
                try {
                    preparedStmt.setString(i+1,tfArray[i].getText());
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            //Find primary columnname from current table.
            ResultSet rsprim= null;
            try {
                rsprim = connection.getMetaData().getPrimaryKeys(cbDatabaseNames.getSelectionModel().getSelectedItem().toString(),null,cbTableNames.getSelectionModel().getSelectedItem().toString());
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            String primarykeyColName="";
            while (true){
                try {
                    if (!rsprim.next()) break;
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                try {
                    primarykeyColName = rsprim.getString("COLUMN_NAME");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }

            }
            int tracer=0;
            for (int i=0;i<a.length;i++){
                if(a[i].equals(primarykeyColName)){
                    tracer = i;
                }
            }
            String valuetocheckk=tfArray[tracer].getText();


            String queryforDup ="select * from " + cbTableNames.getSelectionModel().getSelectedItem().toString()+ " where "+primarykeyColName+"="+valuetocheckk;
            Statement statement = null;
            try {
                statement = connection.createStatement();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }


            ResultSet resultset = null;
            try {
                resultset = statement.executeQuery(queryforDup);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            int count = 0;
            while(true) {
                try {
                    if (!resultset.next()) break;
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                count++;
            }
            if (count > 0) {

                    //throw new SQLException("Duplicate primary key is found");
                    Stage popupwindow=new Stage();
                    popupwindow.initModality(Modality.APPLICATION_MODAL);
                    popupwindow.setTitle("Duplicate Error");
                    Label label1= new Label("Duplicate entry please check primary key values.");
                    Button button1= new Button("Close");
                    button1.setOnAction(x -> popupwindow.close());
                    VBox layout= new VBox(10);
                    layout.getChildren().addAll(label1, button1);
                    layout.setAlignment(Pos.CENTER);
                    Scene scene1= new Scene(layout, 300, 250);
                    popupwindow.setScene(scene1);
                    popupwindow.showAndWait();
            }
            else{
                try {
                    preparedStmt.execute();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }

            }
            try {
                genTable();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        vboxForm.getChildren().add(btSubmit);
    }


    public void updateRow() throws SQLException {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM "+cbTableNames.getSelectionModel().getSelectedItem().toString());

        ResultSetMetaData rsMetaData = rs.getMetaData();
        int colCount = rsMetaData.getColumnCount();
        String[] a = new String[colCount];

        // column nameleri bir arraye al for later use
        for (int i = 1; i < colCount + 1; i++) {
            String columnName = rsMetaData.getColumnName(i);
            a[i-1] = columnName;
        }
        vboxForm.getChildren().clear();
        TextField[] tfArray = new TextField[colCount];

        Label info = new Label();
        info.setText("Please fill all the fields before submitting anything.");
        vboxForm.getChildren().add(info);
        for (int i = 0; i < colCount; i++) {
            HBox hbox = new HBox();

            Label lb = new Label();
            lb.setText(a[i]);
            TextField gentf = new TextField();
            tfArray[i] = gentf;
            hbox.getChildren().addAll(lb,gentf);
            vboxForm.getChildren().add(hbox);
        }
        Button btSubmit = new Button();
        btSubmit.setText("Update");
        btSubmit.setOnAction(e -> {
            //Find primary columnname from current table.
            ResultSet rsprim= null;
            try {
                rsprim = connection.getMetaData().getPrimaryKeys(cbDatabaseNames.getSelectionModel().getSelectedItem().toString(),null,cbTableNames.getSelectionModel().getSelectedItem().toString());
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            String primarykeyColName="";
            while (true){
                try {
                    if (!rsprim.next()) break;
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                try {
                    primarykeyColName = rsprim.getString("COLUMN_NAME");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            String setFields ="";
            for (int i=0;i<a.length;i++){
                setFields=setFields+a[i]+" = ?, ";

            }
            //Primary key'in columnunu bul
            TableColumn colu = getTableColumnByName(tvTable,primarykeyColName);
            //seçili row u al
            TablePosition pos = (TablePosition) tvTable.getSelectionModel().getSelectedCells().get(0);
            int row = pos.getRow();
// seçili rowdaki listeyi çek
            ObservableList<String> item = (ObservableList<String>) tvTable.getItems().get(row);
//silinmesi gereken primary key valuesunu çek
            String valuetoupdate = (String) colu.getCellObservableValue(item).getValue();

            setFields=setFields.substring(0,setFields.length()-2);
            String query = "UPDATE "+cbTableNames.getSelectionModel().getSelectedItem().toString()+ " SET "+ setFields+" WHERE "+primarykeyColName+" = "+valuetoupdate;

            PreparedStatement preparedStmt = null;
            try {
                preparedStmt = connection.prepareStatement(query);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            for (int i=0;i<colCount;i++){
                try {
                    preparedStmt.setString(i+1,tfArray[i].getText());
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            //hangi textfielddaki değer primary key e denk geliyor.
            int tracer=0;
            for (int i=0;i<a.length;i++){
                if(a[i].equals(primarykeyColName)){
                    tracer = i;
                }
            }
            String valuetocheckk=tfArray[tracer].getText();



            String queryforDup ="select * from " + cbTableNames.getSelectionModel().getSelectedItem().toString()+ " where "+primarykeyColName+"="+valuetocheckk;
            Statement statement = null;
            try {
                statement = connection.createStatement();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }


            ResultSet resultset = null;
            try {
                resultset = statement.executeQuery(queryforDup);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            int count = 0;
            while(true) {
                try {
                    if (!resultset.next()) break;
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                count++;
            }
            if (count > 0) {

                Stage popupwindow=new Stage();
                popupwindow.initModality(Modality.APPLICATION_MODAL);
                popupwindow.setTitle("Duplicate Error");
                Label label1= new Label("Duplicate entry please check primary key values.");
                Button button1= new Button("Close");
                button1.setOnAction(x -> popupwindow.close());
                VBox layout= new VBox(10);
                layout.getChildren().addAll(label1, button1);
                layout.setAlignment(Pos.CENTER);
                Scene scene1= new Scene(layout, 300, 250);
                popupwindow.setScene(scene1);
                popupwindow.showAndWait();

            }
            else{
                try {
                    preparedStmt.execute();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            try {
                genTable();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        vboxForm.getChildren().add(btSubmit);
    }
    public void deleteRow() throws SQLException {
        ResultSet rs=connection.getMetaData().getPrimaryKeys(cbDatabaseNames.getSelectionModel().getSelectedItem().toString(),null,cbTableNames.getSelectionModel().getSelectedItem().toString());
        String primarykeyColName="";
        while (rs.next()){
             primarykeyColName = rs.getString("COLUMN_NAME");
        }
        String query1 = "delete from " + cbTableNames.getSelectionModel().getSelectedItem().toString()+ " where " + primarykeyColName +" = ?";
        PreparedStatement preparedStmt = connection.prepareStatement(query1);
        //Primary key'in columnunu bul
        TableColumn colu = getTableColumnByName(tvTable,primarykeyColName);
        //seçili row u al
        TablePosition pos = (TablePosition) tvTable.getSelectionModel().getSelectedCells().get(0);
        int row = pos.getRow();
// seçili rowdaki listeyi çek
        ObservableList<String> item = (ObservableList<String>) tvTable.getItems().get(row);
//silinmesi gereken primary key valuesunu çek
        String valuetodelete = (String) colu.getCellObservableValue(item).getValue();
        //query hazır
        preparedStmt.setString(1,valuetodelete);

        Stage popupwindow=new Stage();
        popupwindow.initModality(Modality.APPLICATION_MODAL);
        popupwindow.setTitle("dbBrowserKK");
        Label label1= new Label("Are you sure to delete selected row ?");
        Button button1= new Button("No");
        Button button2= new Button("Yes");
        button1.setOnAction(x -> popupwindow.close());
        button2.setOnAction(x -> {
            try {
                preparedStmt.execute();
            } catch (SQLException e) {
                Stage popupwindow2=new Stage();
                popupwindow2.initModality(Modality.APPLICATION_MODAL);
                popupwindow2.setTitle("MySQLIntegrityConstraintViolationException");
                Label label11= new Label("Cannot delete or update a parent row: a foreign key constraint fails");
                Button button45= new Button("Close");
                button45.setOnAction(xa -> popupwindow2.close());
                VBox layout= new VBox(10);
                layout.getChildren().addAll(label11, button45);
                layout.setAlignment(Pos.CENTER);
                Scene scene1= new Scene(layout, 300, 250);
                popupwindow2.setScene(scene1);
                popupwindow2.showAndWait();
            }
            popupwindow.close();
        });
        HBox hboxx = new HBox();
        hboxx.getChildren().addAll(button1,button2);
        VBox layout= new VBox(10);
        layout.getChildren().addAll(label1, hboxx);
        layout.setAlignment(Pos.CENTER);
        Scene scene1= new Scene(layout, 300, 250);
        popupwindow.setScene(scene1);
        popupwindow.showAndWait();

        genTable();
    }
    private <T> TableColumn<T, ?> getTableColumnByName(TableView<T> tableView, String name) {
        for (TableColumn<T, ?> col : tableView.getColumns())
            if (col.getText().equals(name)) return col ;
        return null ;
    }
    public void firstbtAction(){
        ObservableList<String> items = tvTable.getItems();
        if (!items.isEmpty()) {
            tvTable.getSelectionModel().selectFirst();
        }

    }
    public void lastbtAction(){
        ObservableList<String> items = tvTable.getItems();
        if (!items.isEmpty()) {
            tvTable.getSelectionModel().selectLast();

        }

    }
    public void nextbtAction(){
        int currentindex = tvTable.getSelectionModel().getSelectedIndex();
        if(lastindex!=-1 && currentindex<lastindex){
            tvTable.getSelectionModel().select(currentindex+1);
        }



    }
    public void prevbtAction(){
        int currentindex = tvTable.getSelectionModel().getSelectedIndex();
        if(firstindex!=-1 && currentindex>firstindex){
            tvTable.getSelectionModel().select(currentindex-1);
        }

    }



    }

