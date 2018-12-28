#!/usr/bin/ruby

def read_keys(path, base_dir)
	keys = {}

	File.open(path).read.scan(/<\s*string\s+name\s*=\s*"([^"]+)"[^>]*?>(.+?)<\s*\/\s*string\s*>/m).each do |item|
		subpath = path.start_with?(base_dir) ? path[base_dir.size, path.size] : path
		puts "Duplicated key \"#{item.first}\" in \"#{subpath}\"" if keys.key?(item.first)
		keys[item.first] = true
	end

	return keys
end

def compare_keys(path, def_keys, base_dir)
	keys = read_keys(path, base_dir)

	def_keys.each_key do |key|
		subpath = path.start_with?(base_dir) ? path[base_dir.size, path.size] : path
		puts "Missing key \"#{key}\" in \"#{subpath}\"" unless keys.key?(key)
	end
end

def process(base_dir)
	def_keys = read_keys("#{base_dir}/values/strings.xml", base_dir)

	Dir.open("#{base_dir}").each do |dname|
		next unless dname.match(/^values-/)
		next unless File.exists?("#{base_dir}/#{dname}/strings.xml")
		compare_keys("#{base_dir}/#{dname}/strings.xml", def_keys, base_dir)
	end
end

puts "[+ Check strings]"
process(File.dirname(__FILE__) + '/../../src/main/res')
puts "[- Check strings]"
