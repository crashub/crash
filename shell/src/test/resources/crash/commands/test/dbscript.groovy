package crash.commands.test
jdbc.connect "jdbc:derby:memory:EmbeddedDB;create=true"
jdbc.execute "create table derbyDB(num int, addr varchar(40))"
jdbc.execute "insert into derbyDB values (1956,'Webster St.')"
jdbc.execute "insert into derbyDB values (1910,'Union St.')"
jdbc.select "* from derbyDb"
jdbc.close