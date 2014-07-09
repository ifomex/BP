conn_id_map = {}
user_list = {}
track_data = nil
server_id = nil
is_start = false
usr_win_ord = 1

--[[

]]
gamooga.onconnect(function(conn_id)
    --gamooga.send(conn_id, "userlist", {oul = online_user_list})
end)

--[[

]]
gamooga.onmessage("user_name", function(conn_id, nick) --nove pripojeny uzivatel
	if is_start then
		return		--pokud hra zacala, uzivatele jiz nelze pridat
	end
	gamooga.send(conn_id, "your_id", {id=conn_id})
	
	id = conn_id					--id uzivatele
	user_list[id] = {}
	user_list[id]["name"] = nick	--jmeno uzivatele
	user_list[id]["stat"] = false
	user_list[id]["p_lat"] = nil		--pozice uzivatele
	user_list[id]["p_lon"] = nil		--pozice uzivatele
	gamooga.broadcast("userlist", user_list)
	
    conn_id_map[conn_id] = nick
    gamooga.send(conn_id, "sendtrack", track_data)		--posle trat (posilajici ji zahodi)
    
end)

gamooga.onmessage("track_data", function(conn_id, data)
    track_data = data		--ulozi trat
    server_id = conn_id
end)

gamooga.onmessage("first_pos", function(conn_id, data)
	--gamooga.broadcast("test", data);
	user_list[conn_id]["stat"] = true
	user_list[conn_id]["p_lat"] = data["p_lat"]
	user_list[conn_id]["p_lon"] = data["p_lon"]
	gamooga.broadcast("userlist", user_list)
	
	sum = true
	for i,v in ipairs(user_list) do 
		sum = sum and v["stat"]
	end
	if sum then
		gamooga.send(server_id, "ready", {ready=true})
	end
end)

gamooga.onmessage("pos", function(conn_id, data)
    --gamooga.broadcastexcept("user_pos", {id=conn_id,loc=data}, conn_id)
    user_list[conn_id]["p_lat"] = data["p_lat"]
    user_list[conn_id]["p_lon"] = data["p_lon"]
    gamooga.broadcast("user_pos", {id=conn_id,loc=data})
end)

gamooga.onmessage("start", function(conn_id, data)
    is_start = true
    gamooga.broadcastexcept("start", data, conn_id)
end)

gamooga.onmessage("win", function(conn_id, data)
	gamooga.send(conn_id, "you_win", {ord=usr_win_ord})
	gamooga.broadcastexcept("usr_win", {name=user_list[conn_id]["name"],ord=usr_win_ord}, conn_id)
	usr_win_ord = usr_win_ord+1 
end)

gamooga.ondisconnect(function(conn_id)
	conn_id_map[conn_id] = nil
	user_list[conn_id] = nil
	gamooga.broadcast("userlist", user_list)
end)
