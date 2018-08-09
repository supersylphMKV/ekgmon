var host = window.location.origin;
var socket = io(host);
var tableBody;
var tableChild =
    '<tr style="display: flex">' +
    '<td class="td_name" style="flex: 5"></td>' +
    '<td class="td_age" style="flex: 1"></td>' +
    '<td class="td_date" style="flex: 2"></td>' +
    '<td style="flex: 1"><button  class="btn_submit" >Lihat Hasil</button></td>' +
    '</tr>';

$(document).ready(SetUp());

function SetUp(){
    tableBody = $('#tableData-body');
    GetDataList();
}

function GetDataList(){
    socket.emit('list',{table : 'r'}, function(err,res){
        console.log(res);
        res.sort(compare);
        if(res){
            var dataKeys = Object.keys(res);
            for(var i = 0; i < dataKeys.length; i++){
                var childELm = $(tableChild);
                $(childELm).find(".td_name").text(res[dataKeys[i]].name);
                $(childELm).find(".td_age").text(res[dataKeys[i]].age);
                $(childELm).find(".td_date").text(res[dataKeys[i]].date);
                $("#tableData-body").append($(childELm));
            }
        }
    })
}

function PreviewData(){

}

function compare(a,b) {
    var ma = MonthToInt(a.date.split(' ')[1]);
    var mb = MonthToInt(b.date.split(' ')[1]);
    var sa = ma + " " + a.date.split(' ').slice(2).join(' ');
    var sb = mb + " " + b.date.split(' ').slice(2).join(' ');
    console.log(sa, sb);
    if (sa < sb)
        return 1;
    if (sa > sb)
        return -1;
    return 0;
}

function MonthToInt(input){
    var month = {
        Jan : 01,
        Feb : 02,
        Mar : 03,
        Apr : 04,
        May : 05,
        Jun : 06,
        Jul : 07,
        Aug : 08,
        Sep : 09,
        Oct : 10,
        Nov : 11,
        Dec : 12
    };

    return month[input];
}