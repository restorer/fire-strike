#!/usr/bin/ruby

require 'nokogiri'

class UnusedResourcesFinder
    def parse_xml_resource(path)
        xml_contents = File.read(path)
        doc = Nokogiri::XML(xml_contents)

        doc.xpath('/resources/*').each do |node|
            category = node.name.gsub(/^.+\-/, '')
            res_id = node['name']

            @existing["#{category}/#{res_id}"] = true

            if category == 'style'
                @used["style/#{node['parent']}"] = true if node['parent']
                res_path = res_id

                while res_path =~ /\./
                    res_path.gsub!(/\.[^.]+?$/, '')
                    @used["#{category}/#{res_path}"] = true
                end
            end
        end

        xml_contents.scan(/@([a-z]+\/[0-9A-Za-z_.]+)/).each do |match|
            @used[match[0]] = true
        end
    end

    def read_resources(dir)
        Dir.open(dir).reject { |subdir| subdir =~ /^\./ }.each do |subdir|
            category = subdir.gsub(/\-.+$/, '')

            Dir.open("#{dir}/#{subdir}").reject { |name| name =~ /^\./ }.each do |name|
                res_id = name.gsub(/\..+$/, '')

                if ['drawable', 'anim', 'color', 'font', 'layout'].include?(category)
                    @existing["#{category}/#{res_id}"] = true
                end

                if name =~ /\.xml$/
                    parse_xml_resource("#{dir}/#{subdir}/#{name}")
                end
            end
        end
    end

    def find_usages_in_code(path)
        Dir.glob("#{path}/**/*.java") do |file_path|
            file_contents = File.read(file_path)

            file_contents.scan(/R\.([a-z]+)\.([0-9A-Za-z_.]+)/).each do |match|
                res_path = "#{match[0]}/#{match[1]}"
                @used[res_path] = true
            end
        end
    end

    def find_unused
        @existing.each_key do |res_path|
            p res_path unless @used.key?(res_path)
        end
    end

    def process(base_dir)
        @base_dir = base_dir
        @existing = {}
        @used = {}

        parse_xml_resource("#{base_dir}/AndroidManifest.xml")
        read_resources("#{base_dir}/res")
        find_usages_in_code("#{base_dir}/java")
        find_unused
    end
end

urf = UnusedResourcesFinder.new
urf.process(File.dirname(__FILE__) + '/../../src/main')
