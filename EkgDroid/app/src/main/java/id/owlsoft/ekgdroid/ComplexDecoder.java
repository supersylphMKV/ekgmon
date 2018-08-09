package id.owlsoft.ekgdroid;

import java.util.ArrayList;
import java.util.List;

public class ComplexDecoder {
    private List<Float> inputData = new ArrayList<>();
    private List<ECGComplex> dataQue = new ArrayList<>();
    private List<EventListener> dataUpdate = new ArrayList<>();
    private int currentDataIdxProcessed = -1;

    public void OnDataUpdateListener(EventListener listener){
        if(!dataUpdate.contains(listener)){
            dataUpdate.add(listener);
        }
    }

    public void OnDataUpdateRemoveListener(EventListener listener){
        if(dataUpdate.contains(listener)){
            dataUpdate.remove(listener);
        }
    }

    public void InputData(float[] data){

        for (float f : data){
            inputData.add(f);
        }

        
    }

    public void DataRead(){

    }

    public class ECGComplex {
        private int _dataLength;
        private int _spIdx, _ppIdx, _epIdx;
        private int _sqIdx, _pqIdx;
        private int _rIdx;
        private int _psIdx, _esIdx;
        private int _stIdx, _ptIdx, _etIdx;
        private int _startDataIdx, _endDataIdx;
        private int _pSegmentLength, _qrsSegmentLength, _tSegmentLength, _complexSegmentLength;
        private float[] _data;
        public boolean isPAbsent, isRInverted, isTAbsent;

        public int get_dataLength(){
            return this._dataLength;
        }

        public int get_startDataIdx(){
            return this._startDataIdx;
        }

        public int get_endDataIdx(){
            return this._endDataIdx;
        }

        public int get_pSegmentLength(){
            return this._pSegmentLength;
        }

        public int get_qrsSegmentLength(){
            return this._qrsSegmentLength;
        }

        public int get_tSegmentLength(){
            return this._tSegmentLength;
        }

        public float[] get_data(){
            return this._data;
        }

    }
}
