USE gitblog;
/* 用户表
 *
 */
create table user (
    user_id bigint unsigned auto_increment comment '用户id',
    user_name varchar(200) not null comment '用户名也要保持唯一',
    user_password char(48) not null comment '用户密码',
    user_email varchar(100) not null comment '用户邮箱',
    user_status tinyint not null comment '用户状态：1-正常（注册，且验证邮箱后）； 0-忘记密码，待重置； -1-已删除; -2-注册了，但未验证邮箱',
    create_time int unsigned not null comment '账户创建时间',
    last_mod_time int unsigned comment '上次修改时间',
    delete_time int unsigned comment '账户删除时间',
    primary key (user_id)
    /*
    如果一个email已经注册过，但是删除了账户（user_status为-1），那么允许这个email继续注册，但是不能看到原来账户的信息。
    所以需要新创建一条user表记录，分配新的user_id。如果update原来的user记录，那么其他表的外键user_id和原来的user关联就有可能
    看到原来的用户信息。虽然其他信息的表应该也把status改为“已删除”状态，但是为了把前后的信息分的清清楚楚，还是新建一个user比较好。

    哪怕按上面的情况没有发生，如果user表有一些基本信息，比如签名。
    新用户注册并没有写，但是使用原来的user记录的话，现在的用户就会看到原来的签名。

    而要创建两个相同的email的话，就不能让数据库来保证唯一性了，而是要在长续重保证唯一性。也就是说，如果email在数据库中已经有记录了，
    但是user_status是已删除，那么就还可以注册新用户，新建一条记录。
    为了用户还能使用这个user_name，原来的的user_name记录的status如果是“已删除”的话，新用户还是可以使用这个user_name的。
    unique key UN (user_name),
    unique key UE (user_email)
    */
)
engine=InnoDB DEFAULT CHARSET=utf8mb4;

/* token表
 * 供用户注册，修改密码，忘记密码功能使用的token
 */
create table user_token (
    token_id bigint unsigned auto_increment comment 'token id',
    token char(36) comment '注册，修改密码，重置密码的url里面带有的token，使用uuid生成',
    user_id bigint unsigned not null comment '用户id',
    token_type tinyint not null comment 'token的用途，是否使用过：11-注册，未使用；12-注册，使用过；21-修改密码，未使用；22-修改密码，使用过；31-忘记密码，未使用；32-忘记密码，使用过',
    create_time int unsigned not null comment '创建token的时间',
    reset_time int unsigned comment '重设密码时间；如果为空说明没有用过，',
    effective_time smallint unsigned comment 'token的有效时间，单位是秒',
    primary key (token_id),
    unique key TK (token),
    foreign key (user_id) references user(user_id)
)
engine=InnoDB DEFAULT CHARSET=utf8mb4;

/* 日志表
 *
 */
create table opt_log (
    log_id bigint unsigned auto_increment comment 'log id',
    opt_name varchar(50) comment '操作名称',
    user_id bigint unsigned not null comment '用户id',
    request_uri varchar(1000) comment '请求的uri',
    method varchar(200) comment '请求访问的controller层方法',
    -- 如果是保存一个博客的内容，那么方法的参数就会非常的大，所以需要能在注解中标注是否要保存这个字段。
    params varchar(500) comment '被访问的方法的传入参数',
    ip varchar(50) comment '用户IP地址',
    user_agent varchar(1000) comment '浏览器UA，从request中的header拿到',
    error_msg varchar(1000) comment '报错信息',
    create_time int unsigned not null comment '日志触发时间',
    execute_time smallint unsigned comment '方法执行所用的时间，毫秒为单位',
    log_status tinyint not null comment '日志状态：1-成功； -1-失败',
    primary key (log_id),
    foreign key (user_id) references user(user_id)
)
engine=InnoDB DEFAULT CHARSET=utf8mb4;