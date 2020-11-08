#!/usr/bin/ruby

raise RuntimeError.new('This script is no longer needed and was left for historical reasons.')

require 'rubygems'
require 'fileutils'
require 'natcmp'
require 'json'

class LevelFixer
    def fix_2_to_3
        @map.each do |row|
            row.each do |item|
                if item['type'] == 4 # TPASS (old)
                    item['type'] = 8 # DLAMP (new)
                    item['value'] = 2 # grass (new)
                elsif item['type'] == 9 # T_OBJ
                    if item['value'] == 17 # dblchaingun (old)
                        item['value'] = 15 # tmp (new)
                    elsif item['value'] == 18 # openmap (old)
                        item['value'] = 19 # openmap (new)
                    elsif item['value'] == 19 # rocket (old)
                        item['value'] = 17 # grenade (new)
                    elsif item['value'] == 20 # rbox (old)
                        item['value'] = 18 # gbox (new)
                    elsif item['value'] == 21 # rlauncher
                        item['value'] = 17 # grenade (new)
                    end
                end
            end
        end

        @level['ensureLevel'] = 6 if @level.key?('ensureLevel') && @level['ensureLevel'] > 6
        @level.delete('ensure')
        @level.delete('monsters')
        @level['format'] = 3
    end

    def fix_3_to_4
        @map.each do |row|
            row.each do |item|
                 if item['type'] == 10 # MON
                    if item['value'] == 6
                        item['value'] = 2
                    elsif item['value'] == 7
                        item['value'] = 3
                    elsif item['value'] == 8
                        item['value'] = 4
                    end
                 end
           end
        end

        @level['format'] = 4
    end

    def fix_level(from_name)
        @level = JSON.parse(File.read(from_name))
        @map = @level['map']

        if @level['format'] == 2
            fix_2_to_3
        elsif @level['format'] == 3
            fix_3_to_4
        else
            puts "Unsupported level format"
            return
        end

        File.open(from_name, 'wb') { |fo| fo << @level.to_json }
    end

    def process
        src_levels_dir = File.dirname(__FILE__) + '/levels'

        list = Dir.open(src_levels_dir) .
            reject { |name| !(name =~ /\.gd2l$/) } .
            map { |name| name.gsub(/\.gd2l$/, '') } .
            sort { |a, b| Natcmp::natcmp(a, b) }

        list.each do |name|
            puts "Fixing #{name}"
            fix_level("#{src_levels_dir}/#{name}.gd2l")
        end
    end
end

lf = LevelFixer.new
lf.process
