var createError = require('http-errors');
var express = require('express');
var path = require('path');
var cookieParser = require('cookie-parser');
var logger = require('morgan');

var indexRouter = require('./routes/index');
var usersRouter = require('./routes/users');

var r = require('rethinkdbdash')({db:'ekg'});

var app = express();

var http    = require('http');
var server  = http.Server(app);
var io      = require('socket.io')(server);

var sc = null;

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'jade');

app.use(logger('dev'));
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

app.use('/', indexRouter);
app.use('/users', usersRouter);


// catch 404 and forward to error handler
app.use(function(req, res, next) {
  next(createError(404));
});

// error handler
app.use(function(err, req, res, next) {
  // set locals, only providing error in development
  res.locals.message = err.message;
  res.locals.error = req.app.get('env') === 'development' ? err : {};

  // render the error page
  res.status(err.status || 500);
  res.render('error');
});

io.on('connection', function (socket) {
    console.log('client connected ' + socket);
    sc = socket;
    socket.on('login', Login);
    socket.on('input', InputData);
    socket.on('list', GetList);
    socket.on('data',GetData);
    socket.on('remove',RemoveData);
});


function Login(query,fn){
    console.log("login request");
    var retObj = {
        err : 'Not Defined',
        res : false
    };
    var datatype = Object.prototype.toString.call(query);
    var userData = {
        userName : 'name',
        password : 'pass'
    };

    if(datatype == '[object String]'){

    } else if(datatype == '[object Object]'){
        if('userName' in query){
            userData.userName = query.userName;
        }
        if('password' in query){
            userData.password = query.password;
        }

        r.table('d').filter({userName : userData.userName}).run(function(err,res){
            if(!err){
                CheckUserData(res);
            }
        });
    }

    function CheckUserData(data){
        if(data == null){
            fn(retObj);
        } else {
            if(datatype == '[object String]'){
                fn(retObj);
            } else {
                if(data.length && userData.userName == data[0].userName){
                    if(userData.password == data[0].password){
                        retObj.err = null;
                        retObj.res = data[0];
                        fn(retObj);
                    } else {
                        retObj.err = 'Wrong Password';
                        fn(retObj);
                    }
                } else {
                    fn(retObj);
                }
            }
        }
    }

}

function InputData(query,fn){
    var table = null;
    var data = null;

    if('table' in query){
        table = query.table;
        if('data' in query){
            data = query.data;
        }
    }

    console.log("Data Input");

    if(table != null && data != null){
        if('id' in data){
            r.table(table).get(data.id).run(function(err,res){
                if(!err){
                    if(res){
                        r.table(table).get(data.id).replace(data).run(function(err,res){
                            fn(err,res);
                        })
                    } else {
                        r.table(table).insert(data).run(function(err,res){
                            fn(err,res);
                        })
                    }

                } else {
                    fn(err,res);
                }
            })
        } else {
            r.table(table).insert(data).run(function(err,res){
                fn(err,res);
            })
        }
    }
}

function GetList(query,fn){
    var table = null;
    var filter = null;
    var cmd = null;

    console.log("Data Get List");

    if('table' in query){
        table = query.table;
    }

    if('filter' in query){
        filter = query.filter;
    }

    if(table != null){
        cmd = r.table(table);
        if(filter != null){
            cmd = r.table(table).filter(filter);
        }
    }

    if(cmd != null){
        cmd.run(function(err,res){
            fn(err,res);
        })
    } else {
        fn('Wrong query',null);
    }


}

function GetData(query,fn){
    var table = null;
    var id = null;

    if('table' in query){
        table = query.table;
    }

    if('id' in query){
        id = query.id;
    }

    if(table != null && id != null){
        r.table(table).get(id).run(function(err,res){
            fn(err,res);
        })
    } else {
        fn({
            err : 'Wrong Query',
            res : null
        })
    }
}

function RemoveData(query,fn){
    var table = null;
    var id = null;

    if('table' in query){
        table = query.table;
    }

    if('id' in query){
        id = query.id;
    }

    if(table != null && id != null){
        r.table(table).get(id).delete().run(function(err,res){
            fn(err,res);
        })
    } else {
        fn({
            err : 'Wrong Query',
            res : null
        })
    }
}


module.exports = {app : app, server : server};
