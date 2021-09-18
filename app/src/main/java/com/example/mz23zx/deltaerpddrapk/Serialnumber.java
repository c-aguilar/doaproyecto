package com.example.mz23zx.deltaerpddrapk;

import android.provider.Settings;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class Serialnumber {

    public Serialnumber(String s) {
        if (s.toUpperCase().startsWith("S")) {
            s = s.substring(1, s.length() - 1);
        }
        _serialnumber = s.toUpperCase();
        Map<String, Object> serial = SQL.Current().GetRecord(String.format("SELECT * FROM vw_Smk_Serials WHERE Serialnumber = '%1$s'", _serialnumber));
        if (serial != null && serial.size() > 0) {
            _partnumber = serial.get("partnumber").toString();
            _description = serial.get("description").toString();
            _quantity = Float.parseFloat(serial.get("originalquantity").toString());
            switch (serial.get("uom").toString().toUpperCase()) {
                case "PC":
                    _uom = RawMaterial.UnitOfMeasure.PC;
                    break;
                case "M":
                    _uom = RawMaterial.UnitOfMeasure.M;
                    break;
                case "FT":
                    _uom = RawMaterial.UnitOfMeasure.FT;
                    break;
                case "LB":
                    _uom = RawMaterial.UnitOfMeasure.LB;
                    break;
                case "KG":
                    _uom = RawMaterial.UnitOfMeasure.KG;
                    break;
                case "L":
                    _uom = RawMaterial.UnitOfMeasure.L;
                    break;
                case "ROL":
                    _uom = RawMaterial.UnitOfMeasure.ROL;
                    break;
                default:
                    _uom = RawMaterial.UnitOfMeasure.PC;
            }

            switch (serial.get("bum").toString().toUpperCase()) {
                case "PC":
                    _bum = RawMaterial.UnitOfMeasure.PC;
                    break;
                case "M":
                    _bum = RawMaterial.UnitOfMeasure.M;
                    break;
                case "FT":
                    _bum = RawMaterial.UnitOfMeasure.FT;
                    break;
                case "LB":
                    _bum = RawMaterial.UnitOfMeasure.LB;
                    break;
                case "KG":
                    _bum = RawMaterial.UnitOfMeasure.KG;
                    break;
                case "L":
                    _bum = RawMaterial.UnitOfMeasure.L;
                    break;
                case "ROL":
                    _bum = RawMaterial.UnitOfMeasure.ROL;
                    break;
                default:
                    _bum = RawMaterial.UnitOfMeasure.PC;
            }


            _date = (Date) serial.get("date");
            _warehouse = serial.get("warehouse").toString();
            _warehousename = serial.get("warehousename").toString();
            _containerid = serial.get("containerid") == null ? "" : serial.get("containerid").toString();
            _sloc = serial.get("sloc") == null ? "" : serial.get("sloc").toString();

            if (serial.get("consumptiontype").toString().equals(ConsumptionType.Partial.toString()))
                _consumption = ConsumptionType.Partial;
            if (serial.get("consumptiontype").toString().equals(ConsumptionType.Service.toString()))
                _consumption = ConsumptionType.Service;
            if (serial.get("consumptiontype").toString().equals(ConsumptionType.Total.toString()))
                _consumption = ConsumptionType.Total;
            if (serial.get("consumptiontype").toString().equals(ConsumptionType.Obsolete.toString()))
                _consumption = ConsumptionType.Obsolete;
            if (serial.get("consumptiontype").toString().equals(ConsumptionType.Mixed.toString()))
                _consumption = ConsumptionType.Mixed;


            _exist = true;
            switch (serial.get("status").toString()) {
                case "N":
                    _status = SerialStatus.New;
                    break;
                case "P":
                    _status = SerialStatus.Pending;
                    break;
                case "S":
                    _status = SerialStatus.Stored;
                    break;
                case "O":
                    _status = SerialStatus.Open;
                    break;
                case "C":
                    _status = SerialStatus.OnCutter;
                    break;
                case "E":
                    _status = SerialStatus.Empty;
                    break;
                case "Q":
                    _status = SerialStatus.Quality;
                    break;
                case "U":
                    _status = SerialStatus.ServiceOnQuality;
                    break;
                case "T":
                    _status = SerialStatus.Tracker;
                    break;
                case "D":
                    _status = SerialStatus.Deleted;
                    _exist = false;
                default:
                    _status = SerialStatus.Deleted;
                    _exist = false;
            }

            _statusname = serial.get("statusdescription").toString();
            _location = serial.get("location") == null ? "" : serial.get("location").toString();
            _weight = Float.parseFloat( serial.get("weight").toString());
            _trucknumber = serial.get("trucknumber") == null ? "" : serial.get("trucknumber").toString();
            _redtag = (Boolean) serial.get("redtag");
            _invoicetrouble = (Boolean) serial.get("invoicetrouble");
            _critical = (Boolean) serial.get("critical");
            _masternumber = serial.get("masternumber") == null ? "" : serial.get("masternumber").toString();
            _linklabel = serial.get("linklabel") == null ? "" : serial.get("linklabel").toString();
            _scanner = serial.get("scanner") == null ? 0 : (Integer) serial.get("scanner");
            //_expirationdate = serial.get("expirationdate") == null ? new Date(2100, 12, 31) :  (Date) serial.get("expirationdate");
            _lastaudit = serial.get("lastaudit") == null ? new Date(2100, 12, 31) : (Date) serial.get("lastaudit");
            _expirationdate =  new Date(2100, 12, 31);
            _lot = serial.get("lot") == null ? "" : serial.get("lot").toString();
            _id = Integer.parseInt( serial.get("id").toString());
            switch (serial.get("materialtype").toString().toLowerCase()) {
                case "tape":
                    _materialtype = RawMaterial.MaterialType.Tape;
                    break;
                case "terminal":
                    _materialtype = RawMaterial.MaterialType.Terminal;
                    break;
                case "component":
                    _materialtype = RawMaterial.MaterialType.Component;
                    break;
                case "cable":
                    _materialtype = RawMaterial.MaterialType.Cable;
                    break;
                case "conduit":
                    _materialtype = RawMaterial.MaterialType.Conduit;
                    break;
                default:
                    _materialtype = RawMaterial.MaterialType.Component;
            }
            _mrp = serial.get("mrp").toString();
            _randomsloc = serial.get("randomsloc").toString();
            _servicesloc = serial.get("servicesloc").toString();
            _dullsloc = serial.get("dullsloc").toString();
            _current_qty = Float.parseFloat( serial.get("currentquantity").toString());
            _ordering_wip_autoincrement = (Boolean) serial.get("orderingwipautoincrement");
            _current_qty_bum = Float.parseFloat( serial.get("currentquantityinbum").toString());
        } else {
            _exist = false;
            _status = SerialStatus.Unexist;
        }
        serial = null;
    }

    public String get_warehouse() {
        return _warehouse;
    }

    public void set_warehouse(String _warehouse) {
        this._warehouse = _warehouse;
    }

    public String get_statusname() {
        return _statusname;
    }

    public void set_statusname(String _statusname) {
        this._statusname = _statusname;
    }

    public SerialStatus get_status() {
        return _status;
    }

    public void set_status(SerialStatus _status) {
        this._status = _status;
        switch (_status) {
            case Stored:
                _statusname = SQL.Current().GetString("Description", "Smk_SerialStatus", "Status", "S");
                break;
            case Open:
                _statusname = SQL.Current().GetString("Description", "Smk_SerialStatus", "Status", "O");
                break;
            case OnCutter:
                _statusname = SQL.Current().GetString("Description", "Smk_SerialStatus", "Status", "C");
                break;
            case Empty:
                _statusname = SQL.Current().GetString("Description", "Smk_SerialStatus", "Status", "E");
                break;
            case New:
                _statusname = SQL.Current().GetString("Description", "Smk_SerialStatus", "Status", "N");
                break;
            case Pending:
                _statusname = SQL.Current().GetString("Description", "Smk_SerialStatus", "Status", "P");
                break;
            case Quality:
                _statusname = SQL.Current().GetString("Description", "Smk_SerialStatus", "Status", "Q");
                break;
            case ServiceOnQuality:
                _statusname = SQL.Current().GetString("Description", "Smk_SerialStatus", "Status", "U");
                break;
            case Tracker:
                _statusname = SQL.Current().GetString("Description", "Smk_SerialStatus", "Status", "T");
                break;
            default:
                _statusname = SQL.Current().GetString("Description", "Smk_SerialStatus", "Status", "D");
        }
    }

    public Float get_current_qty() {
        return _current_qty;
    }

    public void set_current_qty(Float _current_qty) {
        this._current_qty = _current_qty;
    }

    public Float get_quantity() {
        return _quantity;
    }

    public void set_quantity(Float _quantity) {
        this._quantity = _quantity;
    }

    public Float get_weight() {
        return _weight;
    }

    public void set_weight(Float _weight) {
        this._weight = _weight;
    }

    public Float get_current_qty_bum() {
        return _current_qty_bum;
    }

    public void set_current_qty_bum(Float _current_qty_bum) {
        this._current_qty_bum = _current_qty_bum;
    }

    public Boolean get_exist() {
        return _exist;
    }

    public void set_exist(Boolean _exist) {
        this._exist = _exist;
    }

    public Boolean get_redtag() {
        return _redtag;
    }

    public void set_redtag(Boolean _redtag) {
        this._redtag = _redtag;
    }

    public Boolean get_critical() {
        return _critical;
    }

    public void set_critical(Boolean _critical) {
        this._critical = _critical;
    }

    public String get_masternumber() {
        return _masternumber;
    }

    public Boolean get_invoicetrouble() {
        return _invoicetrouble;
    }

    public void set_invoicetrouble(Boolean _invoicetrouble) {
        this._invoicetrouble = _invoicetrouble;
    }

    public Boolean get_ordering_wip_autoincrement() {
        return _ordering_wip_autoincrement;
    }

    public void set_ordering_wip_autoincrement(Boolean _ordering_wip_autoincrement) {
        this._ordering_wip_autoincrement = _ordering_wip_autoincrement;
    }

    public String get_serialnumber() {
        return _serialnumber;
    }

    public void set_serialnumber(String _serialnumber) {
        this._serialnumber = _serialnumber;
    }

    public String get_partnumber() {
        return _partnumber;
    }

    public void set_partnumber(String _partnumber) {
        this._partnumber = _partnumber;
    }

    public String get_description() {
        return _description;
    }

    public void set_description(String _description) {
        this._description = _description;
    }

    public String get_mrp() {
        return _mrp;
    }

    public void set_mrp(String _mrp) {
        this._mrp = _mrp;
    }

    public String get_warehousename() {
        return _warehousename;
    }

    public void set_warehousename(String _warehousename) {
        if (this._warehouse != _warehouse) {
            this._warehousename = _warehousename;
            _warehousename = SQL.Current().GetString("Name", "Smk_Warehouses", "Warehouse", _warehousename);
        }
    }

    public String get_sloc() {
        return _sloc;
    }

    public void set_sloc(String _sloc) {
        this._sloc = _sloc;
    }

    public String get_containerid() {
        return _containerid;
    }

    public void set_containerid(String _containerid) {
        this._containerid = _containerid;
    }

    public String get_location() {
        return _location;
    }

    public void set_location(String _location) {
        this._location = _location;
    }

    public String get_trucknumber() {
        return _trucknumber;
    }

    public void set_trucknumber(String _trucknumber) {
        this._trucknumber = _trucknumber;
    }

    public String get_lot() {
        return _lot;
    }

    public void set_lot(String _lot) {
        this._lot = _lot;
    }

    public Date get_date() {
        return _date;
    }

    public void set_date(Date _date) {
        this._date = _date;
    }

    public Date get_lastauditdate() {
        return _lastaudit;
    }

    public Date get_expirationdate() {
        return _expirationdate;
    }

    public void set_expirationdate(Date _expirationdate) {
        this._expirationdate = _expirationdate;
    }

    public ConsumptionType get_consumption() {
        return _consumption;
    }

    public void set_consumption(ConsumptionType _consumption) {
        this._consumption = _consumption;
    }

    public Integer get_id() {
        return _id;
    }

    public void set_id(Integer _id) {
        this._id = _id;
    }

    public Integer get_lastmovementid() {
        return _lastmovementid;
    }

    public void set_lastmovementid(Integer _lastmovementid) {
        this._lastmovementid = _lastmovementid;
    }

    public Integer get_scanner() {
        return _scanner;
    }

    public void set_scanner(Integer _scanner) {
        this._scanner = _scanner;
    }

    public String get_randomsloc() {
        return _randomsloc;
    }

    public void set_randomsloc(String _randomsloc) {
        this._randomsloc = _randomsloc;
    }

    public String get_servicesloc() {
        return _servicesloc;
    }

    public void set_servicesloc(String _servicesloc) {
        this._servicesloc = _servicesloc;
    }

    public String get_dullsloc() {
        return _dullsloc;
    }

    public void set_dullsloc(String _dullsloc) {
        this._dullsloc = _dullsloc;
    }

    public RawMaterial.UnitOfMeasure get_uom() {
        return _uom;
    }

    public void set_uom(RawMaterial.UnitOfMeasure _uom) {
        this._uom = _uom;
    }

    public RawMaterial.UnitOfMeasure get_bum() {
        return _bum;
    }

    public void set_bum(RawMaterial.UnitOfMeasure _bum) {
        this._bum = _bum;
    }

    public RawMaterial.MaterialType get_materialtype() {
        return _materialtype;
    }

    public void set_materialtype(RawMaterial.MaterialType _materialtype) {
        this._materialtype = _materialtype;
    }

    public String get_linklabel() {
        return _linklabel;
    }

    String _warehouse, _statusname;
    SerialStatus _status;
    Float _current_qty, _quantity, _weight, _current_qty_bum;
    Boolean _exist;
    Boolean _redtag;
    Boolean _critical;
    String _masternumber;
    String _linklabel;
    Boolean _invoicetrouble;
    Boolean _ordering_wip_autoincrement;
    String _serialnumber, _partnumber, _description, _mrp, _warehousename, _sloc, _containerid, _location, _trucknumber, _lot;
    Date _date, _expirationdate, _lastaudit;
    ConsumptionType _consumption;
    Integer _id, _lastmovementid, _scanner;
    String _randomsloc, _servicesloc, _dullsloc;
    RawMaterial.UnitOfMeasure _uom, _bum;
    RawMaterial.MaterialType _materialtype;



    public Boolean UpdateStatus(SerialStatus new_status) {
        Boolean result;
        switch (new_status) {
            case Stored:
                result = SQL.Current().Update("Smk_Serials", "Status", "S", "ID", _id);
                break;
            case Open:
                result = SQL.Current().Update("Smk_Serials", "Status", "O", "ID", _id);
                break;
            case OnCutter:
                result = SQL.Current().Update("Smk_Serials", "Status", "C", "ID", _id);
                break;
            case Empty:
                result = SQL.Current().Update("Smk_Serials", "Status", "E", "ID", _id);
                break;
            case Deleted:
                result = SQL.Current().Update("Smk_Serials", "Status", "D", "ID", _id);
                break;
            case Tracker:
                result = SQL.Current().Update("Smk_Serials", "Status", "T", "ID", _id);
                break;
            case Quality:
                result = SQL.Current().Update("Smk_Serials", "Status", "Q", "ID", _id);
                break;
            case ServiceOnQuality:
                result = SQL.Current().Update("Smk_Serials", "Status", "U", "ID", _id);
                break;
            case New:
                result = SQL.Current().Update("Smk_Serials", "Status", "N", "ID", _id);
                break;
            case Pending:
                result = SQL.Current().Update("Smk_Serials", "Status", "P", "ID", _id);
                break;
            default:
                result = false;
        }
        if (result == true) _status = new_status;
        return true;
    }

    public Boolean UpdateStatus(SerialStatus new_status, String new_location) {
        Boolean result;
        switch (new_status) {
            case Stored:
                result = SQL.Current().Update("Smk_Serials", new String[]{"Status", "Location"}, new Object[]{"S", new_location}, new String[]{"ID"}, new Object[]{_id});
                break;
            case Open:
                result = SQL.Current().Update("Smk_Serials", new String[]{"Status", "Location"}, new Object[]{"O", new_location}, new String[]{"ID"}, new Object[]{_id});
                break;
            case OnCutter:
                result = SQL.Current().Update("Smk_Serials", new String[]{"Status", "Location"}, new Object[]{"C", new_location}, new String[]{"ID"}, new Object[]{_id});
                break;
            case Empty:
                result = SQL.Current().Update("Smk_Serials", new String[]{"Status", "Location"}, new Object[]{"E", new_location}, new String[]{"ID"}, new Object[]{_id});
                break;
            case Deleted:
                result = SQL.Current().Update("Smk_Serials", new String[]{"Status", "Location"}, new Object[]{"D", new_location}, new String[]{"ID"}, new Object[]{_id});
                break;
            case Tracker:
                result = SQL.Current().Update("Smk_Serials", new String[]{"Status", "Location"}, new Object[]{"T", new_location}, new String[]{"ID"}, new Object[]{_id});
                break;
            case Quality:
                result = SQL.Current().Update("Smk_Serials", new String[]{"Status", "Location"}, new Object[]{"Q", new_location}, new String[]{"ID"}, new Object[]{_id});
                break;
            case ServiceOnQuality:
                result = SQL.Current().Update("Smk_Serials", new String[]{"Status", "Location"}, new Object[]{"C", new_location}, new String[]{"ID"}, new Object[]{_id});
                break;
            case New:
                result = SQL.Current().Update("Smk_Serials", new String[]{"Status", "Location"}, new Object[]{"N", new_location}, new String[]{"ID"}, new Object[]{_id});
                break;
            case Pending:
                result = SQL.Current().Update("Smk_Serials", new String[]{"Status", "Location"}, new Object[]{"P", new_location}, new String[]{"ID"}, new Object[]{_id});
                break;
            default:
                result = false;
        }
        if (result == true){
            _status = new_status;
            _location = new_location;
        }
        return true;
    }

    private void UpdateSloc() {
        switch (this._status) {
            case Tracker:
            case Unexist:
            case Deleted:
                _sloc = "";
                break;
            case Stored:
            case Quality:
                _sloc = _randomsloc;
                break;
            case New:
            case Pending:
                _sloc = "0001";
                break;
            case Open:
            case OnCutter:
            case ServiceOnQuality:
                _sloc = _servicesloc;
                break;
            case Empty:
                _sloc = _dullsloc;
                break;
            default:
                _sloc = "";
        }
    }

    public String MovementCode(SerialMovement movement) {
        switch (movement) {
            case ChangeCutter:
                return "CCR";
            case ChangeLocation:
                return "CLN";

            case DeclareEmpty:
                return "DEY";

            case DeclareEmptyByRoute:
                return "DER";

            case DiscountByRoute:
                return "PDR";

            case ManualAdjustment:
                return "MAT";

            case MoverPartialDiscount:
                return "MPD";

            case MoverTransference:
                return "MTE";

            case OpenContainer:
                return "OCR";

            case PartialDiscount:
                return "PDT";

            case ReturnFromEmpty:
                return "RFE";

            case ReturnToRandom:
                return "RTR";

            case StoreContainer:
                return "SCR";

            case ToQuality:
                return "TQY";

            case ToService:
                return "TSE";

            case ToTracker:
                return "TTT";

            case TrackerPartialDiscount:
                return "TPD";

            case TransferRandom:
                return "TRM";

            case TransferRandomToService:
                return "TRS";

            case TransferService:
                return "TTS";

            case ServiceToCutter:
                return "STC";

            case RandomToCutter:
                return "RTC";

            case RewindCableBarrel:
                return "RCB";

            case PicklistPartialDiscount:
                return "PPD";

            case CriticalBinDiscount:
                return "CBD";

            case CycleCountAdjustment:
                return "AUW";

            case LinkOpenSerial:
                return "LOS";

            case ScanAudit:
                return "AUD";

            case DiscountTapeRoute:
                return "DTR";

            default:
                return "";
        }
    }

    private Boolean InsertMovement(SerialMovement movement, Float quantity, String location, Integer seconds) {
        if (SQL.Current().Insert("Smk_SerialMovements", new String[]{"SerialID", "Movement", "Quantity", "Badge", "Location", "Seconds"}, new Object[]{_id, MovementCode(movement), quantity, GlobalVariables.badge, location, seconds}) == true) {
            _lastmovementid = SQL.Current().GetInteger("MAX(ID)", "Smk_SerialMovements", new String[]{"SerialID", "Movement", "Badge"}, new Object[]{_id, MovementCode(movement), GlobalVariables.badge});
            return true;
        } else {
            return false;
        }
    }

    private Boolean InsertMovement(SerialMovement movement, Float quantity, String location, Integer seconds, Boolean get_newID) {
        if (SQL.Current().Insert("Smk_SerialMovements", new String[]{"SerialID", "Movement", "Quantity", "Badge", "Location", "Seconds"}, new Object[]{_id, MovementCode(movement), quantity, GlobalVariables.badge, location, seconds}) == true) {
            if (get_newID == true)
                _lastmovementid = SQL.Current().GetInteger("MAX(ID)", "Smk_SerialMovements", new String[]{"SerialID", "Movement"}, new Object[]{_id, MovementCode(movement)});
            return true;
        } else {
            return false;
        }
    }

    private Boolean InsertTransfer(Integer id_movement, Float quantity, String from_sloc, String to_sloc) {
        if (from_sloc.equals(to_sloc) == true || quantity == 0f) {
            return true;
        } else {
            return SQL.Current().Insert("Smk_SAPTransfers", new String[]{"MovementID", "Quantity", "FromSloc", "ToSloc"}, new Object[]{id_movement, quantity, from_sloc, to_sloc});
        }
    }

    public Boolean ChangeLocation(String new_location) {
        //if (_location.equals(new_location) == false) { //PARA EVITAR QUE LOS OPERADORES HAGAN CAMBIOS SIN SENTIDO
            if (SQL.Current().Update("Smk_Serials", new String[]{"Location"}, new Object[]{new_location}, new String[]{"ID"}, new Object[]{_id}) == true) {
                InsertMovement(SerialMovement.ChangeLocation, 0f, new_location, 0, false);
                _location = new_location;

                if(this.get_warehouse().equals(GlobalFunctions.Warehouse(new_location)) == false) //INSERTAR MOVIMIENTO DE CAMBIO DE ESTACION SI APLICARA
                    if (this.get_status() == SerialStatus.Stored || this.get_status() == SerialStatus.Quality || this.get_status() == SerialStatus.Tracker)
                        this.TransferRandom(GlobalFunctions.Warehouse(new_location));
                    else if (this.get_status() == SerialStatus.Open || this.get_status() == SerialStatus.ServiceOnQuality)
                        this.TransferService(GlobalFunctions.Warehouse(new_location));

                if (this.get_status()==SerialStatus.Stored) //ACTUALIZAR LA FECHA DE AUDITORIA YA QUE LA CAJA ESTA NUEVA Y HACER CAMBIO DE LOCAL SIGINIFICA QUE LA CAJA EXISTE
                    this.UpdateAuditDate();

                return true;
            }
            return false;
        //} else {
        //    return true;
        //}
    }


    public Boolean Store(String location) {
        if (InsertMovement(SerialMovement.StoreContainer, 0f, location, 0, (_consumption == ConsumptionType.Service || _consumption == ConsumptionType.Obsolete) && "0001".equals(this.get_randomsloc()) == false  ? true : false) == true)
            if ((_consumption == ConsumptionType.Service || _consumption == ConsumptionType.Obsolete) && "0001".equals(this.get_randomsloc()) == false)
                if (InsertTransfer(this._lastmovementid,this.get_current_qty(),"0001",this.get_randomsloc()) == true)
                    if (UpdateStatus(SerialStatus.Stored, location) == true){
                        UpdateSloc();

                        if(this.get_warehouse().equals(GlobalFunctions.Warehouse(location)) == false)
                            this.TransferRandom(GlobalFunctions.Warehouse(location));

                        if (_current_qty > 0 && Boolean.parseBoolean(GlobalVariables.Parameters("Smk_AutoMinMaxReport", "")) == false)
                            ReportMinMax(true);
                        return true;
                    }
                    else
                        return false;
                else
                    return false;
            else
                if (UpdateStatus(SerialStatus.Stored, location) == true){
                    UpdateSloc();

                    if(this.get_warehouse().equals(GlobalFunctions.Warehouse(location)) == false)
                        this.TransferRandom(GlobalFunctions.Warehouse(location));

                    if (_current_qty > 0 && Boolean.parseBoolean(GlobalVariables.Parameters("Smk_AutoMinMaxReport", "")) == false)
                        ReportMinMax(true);
                    return true;
                }
                else
                    return false;
        else
            return false;
    }

    /*public Boolean SimpleStore(String location) {
        if (InsertMovement(SerialMovement.StoreContainer, 0f, location, 0, (_consumption == ConsumptionType.Service || _consumption == ConsumptionType.Obsolete) && "0001".equals(this.get_randomsloc()) == false  ? true : false) == true)
            if ((_consumption == ConsumptionType.Service || _consumption == ConsumptionType.Obsolete) && "0001".equals(this.get_randomsloc()) == false)
                if (InsertTransfer(this._lastmovementid,this.get_current_qty(),"0001",this.get_randomsloc()) == true)
                    if (UpdateStatus(SerialStatus.Stored, location) == true)
                        return true;
                    else
                        return false;
                else
                    return false;
            else
                if (UpdateStatus(SerialStatus.Stored, location) == true)
                    return true;
                else
                    return false;
        else
            return false;
    }*/

    public Boolean Open(String new_location) {
        switch (this.get_materialtype()){
            case TerminalAssembly: case Seal: case Component: case Conduit: case Tube: case Tape:
                if (this.get_consumption() == ConsumptionType.Total && Boolean.parseBoolean(GlobalVariables.Parameters("SMK_IMSConsumptionChange","False")) == true && (this.get_uom() == RawMaterial.UnitOfMeasure.PC || SQL.Current().Exists(String.format("SELECT Partnumber FROM Sys_ConversionUnits WHERE Partnumber = '%1$s' AND (BuM = 'PC' OR AuM = 'PC')",this.get_partnumber())))){
                    SQL.Current().Update("Sys_RawMaterial","ConsumptionType","Partial","Partnumber",this.get_partnumber());
                    SQL.Current().Insert("Sys_Log",new String[]{"[User]","[Description]","[KeyWord]"},new Object[]{GlobalVariables.badge,this.get_partnumber(),"SMK_IMSConsumptionChange"});
                    _servicesloc = SQL.Current().GetString("ServiceSloc","vw_Smk_Serials","ID", this.get_id());
                    _consumption = ConsumptionType.Mixed;
                }
                break;
        }

        if (InsertMovement(SerialMovement.OpenContainer, 0f, new_location, 0) == true && InsertTransfer(_lastmovementid, _current_qty, _sloc, _servicesloc) == true) {
            UpdateStatus(SerialStatus.Open, new_location);
            UpdateSloc();

            if(this.get_warehouse().equals(GlobalFunctions.Warehouse(new_location)) == false) //HACER CAMBIO DE ESTACION SI APLICARA
                this.TransferService(GlobalFunctions.Warehouse(new_location));

            if (this.get_current_qty() == this.get_quantity()) //ACTUALIZAR LA FECHA DE AUDITORIA YA QUE LA CAJA ESTABA NUEVA Y HACER ABRIRLA SIGNIFICA QUE EXISTE
                this.UpdateAuditDate();

            CheckFIFO();
            CheckCDRMissing();
            OrderingAutoincrementWIP();
            return true;
        } else {
            return false;
        }
    }

    public Boolean OpenAndLink(String new_location,Integer link_serial_id) {
        switch (this.get_materialtype()){
            case TerminalAssembly: case Seal: case Component: case Conduit: case Tube: case Tape:
                if (this.get_consumption() == ConsumptionType.Total && Boolean.parseBoolean(GlobalVariables.Parameters("SMK_IMSConsumptionChange","False")) == true && (this.get_uom() == RawMaterial.UnitOfMeasure.PC || SQL.Current().Exists(String.format("SELECT Partnumber FROM Sys_ConversionUnits WHERE Partnumber = '%1$s' AND (BuM = 'PC' OR AuM = 'PC')",this.get_partnumber())))){
                    SQL.Current().Update("Sys_RawMaterial","ConsumptionType","Partial","Partnumber",this.get_partnumber());
                    SQL.Current().Insert("Sys_Log",new String[]{"[User]","[Description]","[KeyWord]"},new Object[]{GlobalVariables.badge,this.get_partnumber(),"SMK_IMSConsumptionChange"});
                    _servicesloc = SQL.Current().GetString("ServiceSloc","vw_Smk_Serials","ID", this.get_id());
                    _consumption = ConsumptionType.Mixed;
                }
                break;
        }

        if (InsertMovement(SerialMovement.LinkOpenSerial, 0f, new_location, 0) == true && InsertTransfer(_lastmovementid, _current_qty, _sloc, _servicesloc) == true && SQL.Current().Insert("Smk_LinkLabelMovements", new String[] {"LinkID", "SerialID", "Badge"}, new Object[] {link_serial_id, this.get_id(), GlobalVariables.badge}) == true) {
            UpdateStatus(SerialStatus.Open, new_location);
            UpdateSloc();

            if(this.get_warehouse().equals(GlobalFunctions.Warehouse(new_location)) == false) //HACER CAMBIO DE ESTACION SI APLICARA
                this.TransferService(GlobalFunctions.Warehouse(new_location));

            if (this.get_current_qty() == this.get_quantity()) //ACTUALIZAR LA FECHA DE AUDITORIA YA QUE LA CAJA ESTABA NUEVA Y HACER ABRIRLA SIGNIFICA QUE EXISTE
                this.UpdateAuditDate();

            CheckFIFO();
            CheckCDRMissing();
            OrderingAutoincrementWIP();
            return true;
        } else {
            return false;
        }
    }

    public Boolean LinkSerial(Integer link_serial_id) {
        if (InsertMovement(SerialMovement.LinkOpenSerial, 0f, "", 0) == true &&  SQL.Current().Insert("Smk_LinkLabelMovements", new String[] {"LinkID", "SerialID", "Badge"}, new Object[] {link_serial_id, this.get_id(), GlobalVariables.badge}) == true) {
            return true;
        } else {
            return false;
        }
    }

    public Boolean PartialDiscount(Float quantity) {
        if (InsertMovement(SerialMovement.PartialDiscount, -quantity, "", 0) == true) {
            InsertTransfer(_lastmovementid, quantity, _sloc, _dullsloc);
            if ((_current_qty - quantity == 0) && Boolean.parseBoolean(GlobalVariables.Parameters("Smk_AutoMinMaxReport", "false")) == true)
                ReportMinMax(false);
            _current_qty -= quantity;
            return true;
        } else {
            return false;
        }
    }

    public Boolean DiscountTapeRoute(String board, String mfg_badge, Float quantity) {
        if (InsertMovement(SerialMovement.DiscountTapeRoute, -quantity, "", 0) == true) {
            InsertTransfer(_lastmovementid, quantity, _sloc, _dullsloc);
            SQL.Current().Insert("SMK_TapeDiscount",new String[] {"Board", "Badge", "MovementID"}, new Object[]{board, mfg_badge, _lastmovementid});

            //if ((_current_qty - quantity == 0) && Boolean.parseBoolean(GlobalVariables.Parameters("Smk_AutoMinMaxReport", "false")) == true)
            //    ReportMinMax(false);
            _current_qty -= quantity;
            return true;
        } else {
            return false;
        }
    }

        public Boolean CriticalBinDiscount(Float quantity,Integer kanban_id) {
            if (InsertMovement(SerialMovement.CriticalBinDiscount, -quantity, GlobalFunctions.Right("00000000" + kanban_id.toString(),8), 0) == true) {
                InsertTransfer(_lastmovementid, quantity, _sloc, _dullsloc);
                SQL.Current().Execute(String.format("UPDATE DDR_CartsLoopKanbans SET [Status] = 'F' WHERE Kanban = %1$s AND [Status] = 'C'",kanban_id.toString()));
                if ((_current_qty - quantity == 0) && Boolean.parseBoolean(GlobalVariables.Parameters("Smk_AutoMinMaxReport", "false")) == true)
                    ReportMinMax(false);
                _current_qty -= quantity;
                return true;
        } else {
            return false;
        }
    }

    public Boolean CycleCountAdjustment(Float new_quantity) {
        float adjustment = new_quantity - this.get_current_qty();
        if (InsertMovement(SerialMovement.CycleCountAdjustment, adjustment, "", 0) == true) {
            this.UpdateAuditDate();
            if (adjustment >= 0)
                InsertTransfer(_lastmovementid, adjustment, _dullsloc, _sloc); //regresar inventario
            else
                InsertTransfer(_lastmovementid, Math.abs(adjustment), _sloc, _dullsloc); //mandar a wip
            return true;
        } else {
            return false;
        }
    }

    public Boolean Empty() {
        if (InsertMovement(SerialMovement.DeclareEmpty, -_current_qty, "", 0) == true) {
            InsertTransfer(_lastmovementid, _current_qty, _sloc, _dullsloc);
            UpdateStatus(SerialStatus.Empty);
            if (Boolean.parseBoolean(GlobalVariables.Parameters("Smk_AutoMinMaxReport", "false")) && _current_qty > 0)
                ReportMinMax(false);
            _current_qty = 0f;
            UpdateSloc();
            return true;
        } else {
            return false;
        }
    }

    private Boolean TransferRandom( String new_warehouse) {
        if (InsertMovement(SerialMovement.TransferRandom, 0f, this.get_location(), 0) == true) {
                if (this.get_sloc().equals("") == false)
                    InsertTransfer(_lastmovementid, this.get_current_qty(), this.get_sloc(), GetRandomSloc(new_warehouse));
                this.set_warehouse(new_warehouse);
                UpdateSloc();
            return true;
        } else {
            return false;
        }
    }

    private Boolean TransferService(String new_warehouse) {
        if (InsertMovement(SerialMovement.TransferService, 0f, this.get_location(), 0) == true) {
            InsertTransfer(_lastmovementid, _current_qty, _sloc, GetServiceSloc(new_warehouse));
            this.set_warehouse(new_warehouse);
            UpdateSloc();
            return true;
        } else {
            return false;
        }
    }

    private Boolean TransferRandomToService(String new_warehouse) {
        switch (this.get_materialtype()){
            case TerminalAssembly: case Seal: case Component: case Conduit: case Tube: case Tape:
                if (this.get_consumption() == ConsumptionType.Total && Boolean.parseBoolean(GlobalVariables.Parameters("SMK_IMSConsumptionChange","False")) == true && (this.get_uom() == RawMaterial.UnitOfMeasure.PC || SQL.Current().Exists(String.format("SELECT Partnumber FROM Sys_ConversionUnits WHERE Partnumber = '%1$s' AND (BuM = 'PC' OR AuM = 'PC')",this.get_partnumber())))){
                    SQL.Current().Update("Sys_RawMaterial","ConsumptionType","Partial","Partnumber",this.get_partnumber());
                    SQL.Current().Insert("",new String[]{"User","Description","KeyWord"},new Object[]{GlobalVariables.badge,this.get_partnumber(),"SMK_IMSConsumptionChange"});
                    _servicesloc = SQL.Current().GetString("ServiceSloc","vw_Smk_Serials","ID", this.get_id());
                }
                break;
        }

        if (InsertMovement(SerialMovement.TransferRandomToService, 0f, this.get_location(), 0) == true) {
            InsertTransfer(_lastmovementid, _current_qty, _sloc, GetServiceSloc(new_warehouse));
            this.UpdateStatus(SerialStatus.Open,this.get_location());
            this.set_warehouse(new_warehouse);
            _status = SerialStatus.Open;
            UpdateSloc();
            CheckCDRMissing();
            return true;
        } else {
            return false;
        }
    }

    public Boolean ReturnToRandom(String location) {
        if (InsertMovement(SerialMovement.ReturnToRandom, 0f, location, 0) == true) {
            InsertTransfer(_lastmovementid, _current_qty, _servicesloc, _randomsloc);
            UpdateStatus(SerialStatus.Stored, location);
            UpdateSloc();
            return true;
        } else {
            return false;
        }
    }

    public Boolean ReturnEmptyToRandom(String location) { //REGRESA TODO EL STDPACK A RESERVA
        if (InsertMovement(SerialMovement.ReturnToRandom, _quantity, location, 0) == true) {
            InsertTransfer(_lastmovementid, _quantity, _dullsloc, _randomsloc);
            UpdateStatus(SerialStatus.Stored, location);
            UpdateSloc();
            return true;
        } else {
            return false;
        }
    }

    public Boolean ReturnServiceToRandom(String location) { //REGRESA_TODO EL STDPACK A RESERVA
        if (InsertMovement(SerialMovement.ReturnFromEmpty, _quantity - _current_qty, "", 0) == true) { //PRIMERO COMPLETAR LA CANTIDAD PARCIAL CONSUMIDA
            InsertTransfer(_lastmovementid, _quantity - _current_qty, _dullsloc, _servicesloc);
            if (InsertMovement(SerialMovement.ReturnToRandom, _quantity, location, 0) == true) //DESPUES REGRESAR_TODO EL STDPACK  A RESERVA
                InsertTransfer(_lastmovementid, _quantity, _servicesloc, _randomsloc);

            UpdateStatus(SerialStatus.Stored, location);
            UpdateSloc();
            return true;
        } else {
            return false;
        }
    }



    public Boolean ReturnFromEmpty() { //REACTIVA LA SERIE CON LA ULTIMA CANTIDAD
        Float qty = SQL.Current().GetFloat(String.format("SELECT TOP 1 ABS(Quantity) FROM Smk_SerialMovements WHERE SerialID = %1$s AND Movement = '%2$s' ORDER BY [Date] DESC", _id, MovementCode(SerialMovement.DeclareEmpty)));
        if (InsertMovement(SerialMovement.ReturnFromEmpty, qty, "", 0) == true) {
            if (_consumption == ConsumptionType.Total) {
                InsertTransfer(_lastmovementid, qty, _sloc, _randomsloc);
                _current_qty = qty;
                UpdateStatus(SerialStatus.Stored);
                UpdateSloc();
            } else {
                InsertTransfer(_lastmovementid, qty, _sloc, _servicesloc);
                _current_qty = qty;
                UpdateStatus(SerialStatus.Open);
                UpdateSloc();
            }
            return true;
        } else {
            return false;
        }
    }

    public Boolean ReactiveSerialToService() { //REACTIVA LA SERIE SIN CANTIDAD
        if (InsertMovement(SerialMovement.ReturnFromEmpty, 0f, "", 0) == true) {
            UpdateStatus(SerialStatus.Open);
            UpdateSloc();
            return true;
        } else {
            return false;
        }
    }

    public Boolean MoverPartialDiscount(Float quantity, Integer mover_part_id, Integer mover_id) {
        if (InsertMovement(SerialMovement.MoverPartialDiscount, -quantity, GlobalFunctions.Right("00000000" + mover_id.toString(), 8), 0) == true) {
            InsertTransfer(_lastmovementid, quantity, _sloc, _dullsloc);
            SQL.Current().Insert("Smk_MoverSerialDiscount", new String[]{"MoverPartID", "SerialMovementID"}, new Object[]{mover_part_id, _lastmovementid});
            _current_qty -= quantity;
            return true;
        } else {
            return false;
        }
    }

    public Boolean MoverPartialDiscount(Float quantity, Integer mover_part_id, Integer mover_id, String to_sloc) {
        if (InsertMovement(SerialMovement.MoverPartialDiscount, -quantity, GlobalFunctions.Right("00000000" + mover_id.toString(), 8), 0) == true) {
            InsertTransfer(_lastmovementid, quantity, _sloc, _dullsloc);
            SQL.Current().Insert("Smk_MoverSerialDiscount", new String[]{"MoverPartID", "SerialMovementID"}, new Object[]{mover_part_id, _lastmovementid});
            _current_qty -= quantity;
            //'Mover la cantidad de inventario al sloc segun el tipo de Mover
            InsertMovement(SerialMovement.MoverTransference, 0f, GlobalFunctions.Right("00000000" + mover_id.toString(), 8), 0);
            InsertTransfer(_lastmovementid, quantity, _dullsloc, to_sloc);
            return true;
        } else {
            return false;
        }
    }

    public Boolean DDRPartialDiscount(Float quantity, Integer kanban_loop_id) {
        if (InsertMovement(SerialMovement.DiscountByRoute, -quantity,"", 0) == true) {
            InsertTransfer(_lastmovementid, quantity, _sloc, _dullsloc);
            SQL.Current().Insert("Smk_DDRSerialDiscount", new String[]{"KanbanLoopID", "SerialMovementID"}, new Object[]{kanban_loop_id, _lastmovementid});
            _current_qty -= quantity;
            return true;
        } else {
            return false;
        }
    }

    public Boolean ToTracker(String location, String reason, String delivery, String comment) {
        if (SQL.Current().Update("Smk_Serials", new String[]{"Status", "InvoiceTrouble", "Location"}, new Object[]{"T", 1, location}, new String[]{"ID"}, new Object[]{_id}) == true) {
            InsertMovement(SerialMovement.ToTracker, 0f, location, 0, true);
            SQL.Current().Insert("Rec_TrackerDetails", new String[]{"MovementID", "Reason", "Delivery", "Comment"}, new Object[]{_lastmovementid, reason, delivery, comment});
            _invoicetrouble = true;
            _status = SerialStatus.Tracker;
            _location = location;
            return true;
        } else {
            return false;
        }
    }

    public Boolean TrackerPartialDiscount(Float quantity) {
        if (InsertMovement(SerialMovement.TrackerPartialDiscount, -quantity, "", 0, true) == true) {
            if (_current_qty - quantity == 0) ReportMinMax(false);
            //EJECUTAR ESTA ACCION ANTES DE DESCONTAR LA CANTIDAD A LA SERIE, NO EDITAR
            _current_qty -= quantity;
            return true;
        } else {
            return false;
        }
    }

    public Boolean ScanAudit() {
        if (InsertMovement(SerialMovement.ScanAudit, 0.0f, _location, 0, false) == true) {
           this.UpdateAuditDate();
            return true;
        } else {
            return false;
        }
    }

    public Boolean ToQuality(String location) {
        switch (_status) {
            case Open:
            case OnCutter:
            case ServiceOnQuality:
                if (SQL.Current().Update("Smk_Serials", new String[]{"Status", "RedTag", "Location"}, new Object[]{"U", 1, location}, new String[]{"ID"}, new Object[]{_id}) == true) {
                    InsertMovement(SerialMovement.ToQuality, 0f, location, 0, false);
                    _redtag = true;
                    _status = SerialStatus.ServiceOnQuality;
                    _location = location;
                    return true;
                } else {
                    return false;
                }
            default:
                if (SQL.Current().Update("Smk_Serials", new String[]{"Status", "RedTag", "Location"}, new Object[]{"Q", 1, location}, new String[]{"ID"}, new Object[]{_id}) == true) {
                    InsertMovement(SerialMovement.ToQuality, 0f, location, 0, false);
                    _redtag = true;
                    _status = SerialStatus.Quality;
                    _location = location;
                    return true;
                } else {
                    return false;
                }
        }
    }


    public String GetRandomSloc(String warehouse) {
        return SQL.Current().GetString("RandomSloc", "Smk_SAPSlocs", new String[]{"Warehouse", "Process"}, new Object[]{warehouse, _consumption.toString()});
    }

    public String GetServiceSloc(String warehouse) {
        return SQL.Current().GetString("ServiceSloc", "Smk_SAPSlocs", new String[]{"Warehouse", "Process"}, new Object[]{warehouse, _consumption.toString()});
    }

    public String GetDullSloc(String warehouse) {
        return SQL.Current().GetString("DullSloc", "Smk_SAPSlocs", new String[]{"Warehouse", "Process"}, new Object[]{warehouse, _consumption.toString()});
    }


    public void CheckFIFO() {
        FIFOThread fth = new FIFOThread(this._partnumber, this._serialnumber, this._warehouse);
        fth.start();
    }

    public class FIFOThread extends Thread {
        String _partnumber, _serialnumber, _warehouse;
        public  FIFOThread(String partnumber, String serialnumber, String warehouse){
            this._partnumber = partnumber;
            this._serialnumber = serialnumber;
            this._warehouse = warehouse;
        }
        @Override
        public void run() {
            String nextserial  = RawMaterial.NextFIFO(this._partnumber, this._warehouse);
           if (nextserial.equals(this._serialnumber) != true) {
               SQL.Current().Insert("Smk_BrokenFIFO", new String[]{"TakenSerialnumber", "NextSerialnumber", "Badge"}, new Object[]{this._serialnumber, nextserial, GlobalVariables.badge});
           }
        }
    }

    public void CheckCDRMissing() {
        CDRMissingThread mth = new CDRMissingThread(this._id, this._partnumber);
        mth.start();
    }

    public class CDRMissingThread extends Thread {
        String _partnumber;
        Integer _id;
        public CDRMissingThread(Integer id, String partnumber){
            this._id = id;
            this._partnumber = partnumber;
        }
        @Override
        public void run() {
            SQL.Current().Update("Smk_MissingAlerts", new String[] {"Active", "AnswerBy", "Answer", "SerialID"}, new Object[] {0,GlobalVariables.badge, "Surtido por supermercado.", this._id}, new String[] {"Partnumber", "Active"}, new Object[] {this._partnumber, 1});
        }
    }

    private void OrderingAutoincrementWIP() {
        if (_consumption == ConsumptionType.Total && _ordering_wip_autoincrement && _current_qty > 0){
            AutoincrementWIPThread aith = new AutoincrementWIPThread(this._partnumber,this._current_qty,this._uom.toString());
            aith.start();
        }
    }

    public void ReportMinMax(Boolean is_store) {
        MinMaxThread mmth = new MinMaxThread(is_store,this._serialnumber,this._current_qty,this._uom.toString(),this._partnumber);
        mmth.start();
    }

    public class AutoincrementWIPThread extends Thread {
        String _partnumber, _uom;
        Float _current_qty;
        public AutoincrementWIPThread(String partnumber,Float current_qty, String uom)
        {
            this._partnumber = partnumber;
            this._uom = uom;
            this._current_qty = current_qty;
        }
        @Override
        public void run() {
            SQL.Current().Execute(String.format("EXEC sp_CR_IncrementWIP '%1$s', %2$s, '%3$s';", this._partnumber, this._current_qty, this._uom));
        }
    }

    public class MinMaxThread extends Thread{
        Boolean _is_store;
        String _serialnumber, _uom, _partnumber;
        Float _current_qty;
        public MinMaxThread(Boolean is_store,String serialnumber,Float current_qty,String uom,String partnumber){
            this._is_store = is_store;
            this._serialnumber = serialnumber;
            this._uom = uom;
            this._partnumber = partnumber;
            this._current_qty = current_qty;
        }
        @Override
        public void run() {
            SQL.Current().Execute(String.format("EXEC sp_Smk_ReportMinMax '%1$s',%2$s,'%3$s','%4$s',%5$s;", this._serialnumber, this._current_qty, this._uom, this._partnumber, this._is_store == true ? "1" : "0"));
        }
    }

    public void UpdateAuditDate(){
        SQL.Current().Execute("UPDATE Smk_Serials SET LastAudit = GETDATE() WHERE ID = " + _id);
    }

    public enum ConsumptionType{
        Mixed,
        Total,
        Partial,
        Obsolete,
        Service
    }

    public enum SerialStatus{
        New,
        Pending,
        Stored,
        Open,
        OnCutter,
        Empty,
        Deleted,
        Unexist,
        Quality,
        ServiceOnQuality,
        Tracker,
    }
    public enum SerialMovement {
        StoreContainer,
        OpenContainer,
        PicklistPartialDiscount,
        MoverPartialDiscount,
        MoverTransference,
        PartialDiscount,
        TrackerPartialDiscount,
        DiscountByRoute,
        DeclareEmpty,
        DeclareEmptyByRoute,
        TransferRandom,
        TransferService,
        TransferRandomToService,
        ChangeLocation,
        ChangeCutter,
        ToService,
        ReturnToRandom,
        ReturnFromEmpty,
        ManualAdjustment,
        ToTracker,
        ToQuality,
        ServiceToCutter,
        RandomToCutter,
        RewindCableBarrel,
        CriticalBinDiscount,
        CycleCountAdjustment,
        LinkOpenSerial,
        ScanAudit,
        DiscountTapeRoute
    }

}
