require 'socket'
require 'google/protobuf'
require_relative 'protobuf_definitions/capacity_pb'

class AdminClient
  SERVERS = {
    1 => 7001,
    2 => 7002,
    3 => 7003
  }

  def initialize
    @server_status = {}
  end

  # Tek bir sunucunun kapasitesini sorgula
  def query_capacity(server_id)
    port = SERVERS[server_id]
    raise "Invalid server ID" unless port

    socket = TCPSocket.new('localhost', port)

    # Capacity sorgusu oluştur
    capacity_query = Protobuf::CapacityRequest.new(
      server_id: server_id
    )

    # Sorguyu gönder
    socket.write(capacity_query.to_proto)

    # Yanıtı al
    response_data = socket.read
    capacity_response = Protobuf::Capacity.decode(response_data)

    # Yanıtı kaydet
    @server_status[server_id] = {
      status: capacity_response.server_status,
      timestamp: capacity_response.timestamp
    }

    socket.close

    puts "Server #{server_id} Status:"
    puts "  Subscriber Count: #{capacity_response.server_status}"
    puts "  Last Updated: #{Time.at(capacity_response.timestamp.seconds)}"
  end

  # Tüm sunucuları sorgula
  def query_all_servers
    puts "\nQuerying all servers..."
    SERVERS.keys.each do |server_id|
      begin
        query_capacity(server_id)
      rescue => e
        puts "Error querying server #{server_id}: #{e.message}"
      end
    end
    puts "\nAll servers queried.\n"
  end

  # Admin ana döngüsü
  def run
    loop do
      puts "\nOptions:"
      puts "1. Query single server capacity"
      puts "2. Query all servers' capacities"
      puts "3. Exit"
      print "Choose an option: "

      input = gets.chomp

      case input
      when '1'
        print "Enter server ID (1-3): "
        server_id = gets.chomp.to_i
        begin
          query_capacity(server_id)
        rescue => e
          puts "Error: #{e.message}"
        end
      when '2'
        query_all_servers
      when '3'
        puts "Exiting admin client."
        break
      else
        puts "Invalid option. Please try again."
      end
    end
  end
end

# Admin Client başlatılıyor
AdminClient.new.run
